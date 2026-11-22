package com.campusrush.ui;

import com.campusrush.exception.AppException;
import com.campusrush.model.PlayerProfile;
import com.campusrush.service.PlayerService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

public final class ProfilePanel extends JPanel {

    private final PlayerService playerService;
    private final Consumer<PlayerProfile> onUse;
    private final Consumer<PlayerProfile> onDelete;
    private final Runnable onBack;
    private final DefaultListModel<PlayerProfile> profileModel =
            new DefaultListModel<>();
    private final JList<PlayerProfile> profileList = new JList<>(profileModel);
    private final JTextField nameField = new JTextField();
    private final JLabel statusLabel = new JLabel(" ");

    public ProfilePanel(PlayerService playerService,
                        Consumer<PlayerProfile> onUse,
                        Consumer<PlayerProfile> onDelete,
                        Runnable onBack) {
        this.playerService = playerService;
        this.onUse = onUse;
        this.onDelete = onDelete;
        this.onBack = onBack;
        buildUi();
    }

    public void refresh() {
        statusLabel.setText("Loading profiles...");
        new SwingWorker<List<PlayerProfile>, Void>() {
            @Override
            protected List<PlayerProfile> doInBackground() {
                return playerService.listProfiles();
            }

            @Override
            protected void done() {
                try {
                    List<PlayerProfile> profiles = get();
                    profileModel.clear();
                    profiles.forEach(profileModel::addElement);
                    statusLabel.setText(profiles.size()
                            + " profile(s) loaded.");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Loading was interrupted.");
                } catch (ExecutionException exception) {
                    statusLabel.setText(message(exception));
                }
            }
        }.execute();
    }

    public void showMessage(String message) {
        statusLabel.setText(message);
    }

    private void buildUi() {
        setLayout(new BorderLayout(18, 18));
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(26, 32, 26, 32));

        JLabel heading = new JLabel("PLAYER PROFILES");
        heading.setFont(Theme.pixelFont(24, true));
        heading.setForeground(Theme.NAVY);
        add(heading, BorderLayout.NORTH);

        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profileList.setFont(Theme.pixelFont(15, false));
        profileList.setBackground(ColorPalette.LIST_BACKGROUND);
        profileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof PlayerProfile profile) {
                    setText((index + 1) + ". " + profile.displayName());
                }
                return this;
            }
        });
        profileList.addListSelectionListener(event -> {
            PlayerProfile selected = profileList.getSelectedValue();
            if (selected != null) {
                nameField.setText(selected.displayName());
            }
        });
        add(new JScrollPane(profileList), BorderLayout.CENTER);

        JPanel controls = new JPanel(new BorderLayout(10, 10));
        controls.setOpaque(false);
        JPanel edit = new JPanel(new BorderLayout(8, 8));
        edit.setOpaque(false);
        edit.add(new JLabel("Name:"), BorderLayout.WEST);
        edit.add(nameField, BorderLayout.CENTER);
        controls.add(edit, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(1, 5, 8, 0));
        buttons.setOpaque(false);
        buttons.add(button("CREATE", this::create));
        buttons.add(button("UPDATE", this::update));
        buttons.add(button("USE SELECTED", this::useSelected));
        buttons.add(button("DELETE", this::deleteSelected));
        buttons.add(button("BACK", onBack));
        controls.add(buttons, BorderLayout.CENTER);

        statusLabel.setFont(Theme.pixelFont(12, false));
        statusLabel.setForeground(Theme.NAVY);
        controls.add(statusLabel, BorderLayout.SOUTH);
        add(controls, BorderLayout.SOUTH);
    }

    private JButton button(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setFont(Theme.pixelFont(11, true));
        Theme.styleButton(button, Theme.SKY_LIGHT, Theme.NAVY);
        button.setMargin(new Insets(9, 6, 9, 6));
        button.addActionListener(event -> action.run());
        return button;
    }

    private void create() {
        String name = nameField.getText();
        runProfileTask(() -> playerService.createProfile(name),
                "Profile created.");
    }

    private void update() {
        PlayerProfile selected = profileList.getSelectedValue();
        if (selected == null) {
            statusLabel.setText("Select a profile to update.");
            return;
        }
        String name = nameField.getText();
        runProfileTask(() -> playerService.updateProfile(
                        selected.id(), name), "Profile updated.");
    }

    private void useSelected() {
        PlayerProfile selected = profileList.getSelectedValue();
        if (selected == null) {
            statusLabel.setText("Select a profile first.");
            return;
        }
        onUse.accept(selected);
        statusLabel.setText("Selected " + selected.displayName() + ".");
    }

    private void deleteSelected() {
        PlayerProfile selected = profileList.getSelectedValue();
        if (selected == null) {
            statusLabel.setText("Select a profile to delete.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete " + selected.displayName()
                        + " and all of its saved scores?",
                "Confirm profile deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return playerService.deleteProfile(selected.id());
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        profileModel.removeElement(selected);
                        nameField.setText("");
                        onDelete.accept(selected);
                        statusLabel.setText("Profile and saved scores deleted.");
                    } else {
                        statusLabel.setText("Profile was not found.");
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Deletion was interrupted.");
                } catch (ExecutionException exception) {
                    statusLabel.setText(message(exception));
                }
            }
        }.execute();
    }

    private void runProfileTask(ProfileTask task, String successMessage) {
        statusLabel.setText("Saving...");
        new SwingWorker<PlayerProfile, Void>() {
            @Override
            protected PlayerProfile doInBackground() {
                return task.run();
            }

            @Override
            protected void done() {
                try {
                    PlayerProfile profile = get();
                    refresh();
                    profileList.setSelectedValue(profile, true);
                    statusLabel.setText(successMessage);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Operation was interrupted.");
                } catch (ExecutionException exception) {
                    statusLabel.setText(message(exception));
                }
            }
        }.execute();
    }

    private String message(Exception exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof AppException appException) {
            return appException.getMessage();
        }
        return "Database error. Check the connection and try again.";
    }

    @FunctionalInterface
    private interface ProfileTask {
        PlayerProfile run();
    }

    private static final class ColorPalette {
        private static final java.awt.Color LIST_BACKGROUND =
                new java.awt.Color(255, 253, 241);
    }
}
