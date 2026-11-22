from pathlib import Path
from datetime import date

from PIL import Image, ImageDraw, ImageFont
from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.table import WD_CELL_VERTICAL_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt, RGBColor


ROOT = Path(__file__).parent
DOCS = ROOT / "docs"
FIGURES = DOCS / "figures"
EVIDENCE = ROOT / "evidence"
EVIDENCE_FIGURES = EVIDENCE / "figures"
FIGURES.mkdir(parents=True, exist_ok=True)
OUTPUT = ROOT / "CampusRush_Documentation.docx"

RELEASE = "1.0.0"
STUDENTS = "Joshua Adam James Bebbington\nBrandon Vladimir B. Cases"
SECTION = "DS21"
REPOSITORY = "https://github.com/jshbebbington/CampusRush"


class EvidenceImages:
    def __init__(self, *paths, width=1.75):
        self.paths = tuple(Path(path) for path in paths)
        self.width = width


def font(size=18, bold=False):
    try:
        return ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", size)
    except OSError:
        return ImageFont.load_default()


def draw_box(draw, xy, label, fill, text_fill=(255, 255, 255), size=18):
    x1, y1, x2, y2 = xy
    draw.rounded_rectangle(xy, radius=14, fill=fill, outline=(20, 33, 48), width=3)
    text_font = font(size, True)
    bbox = draw.multiline_textbbox((0, 0), label, font=text_font, spacing=4, align="center")
    tx = (x1 + x2 - (bbox[2] - bbox[0])) / 2
    ty = (y1 + y2 - (bbox[3] - bbox[1])) / 2 - 2
    draw.multiline_text((tx, ty), label, font=text_font, fill=text_fill, spacing=4, align="center")


def arrow(draw, start, end, fill=(20, 33, 48)):
    draw.line([start, end], fill=fill, width=5)
    x1, y1 = start
    x2, y2 = end
    if abs(x2 - x1) >= abs(y2 - y1):
        direction = 1 if x2 >= x1 else -1
        points = [(x2, y2), (x2 - 14 * direction, y2 - 9),
                  (x2 - 14 * direction, y2 + 9)]
    else:
        direction = 1 if y2 >= y1 else -1
        points = [(x2, y2), (x2 - 9, y2 - 14 * direction),
                  (x2 + 9, y2 - 14 * direction)]
    draw.polygon(points, fill=fill)


def make_architecture():
    image = Image.new("RGB", (1500, 620), (255, 248, 224))
    draw = ImageDraw.Draw(image)
    draw.text((45, 25), "Campus Rush Architecture", fill=(20, 33, 48), font=font(28, True))
    draw_box(draw, (55, 130, 340, 460), "Swing UI\nMainFrame\nPanels", (239, 111, 89), size=22)
    draw_box(draw, (430, 130, 715, 460), "Services\nValidation\nBusiness rules", (107, 82, 170), size=22)
    draw_box(draw, (805, 130, 1090, 460), "DAOs\nPreparedStatement\ntry-with-resources", (27, 166, 79), size=22)
    draw_box(draw, (1180, 130, 1445, 460), "Oracle\nPLAYERS\nSCORES", (25, 48, 74), size=22)
    arrow(draw, (340, 295), (430, 295))
    arrow(draw, (715, 295), (805, 295))
    arrow(draw, (1090, 295), (1180, 295))
    draw.text((55, 510), "GameEngine remains independent of Swing and JDBC; SwingWorker keeps database work off the EDT.", fill=(20, 33, 48), font=font(19, False))
    image.save(FIGURES / "architecture.png")


def make_data_flow():
    image = Image.new("RGB", (1500, 520), (235, 255, 252))
    draw = ImageDraw.Draw(image)
    draw.text((45, 25), "Score Save Data Flow", fill=(20, 33, 48), font=font(28, True))
    boxes = [
        ((55, 170, 280, 360), "Game over\nlistener", (239, 111, 89)),
        ((365, 170, 590, 360), "SwingWorker\ndoInBackground", (107, 82, 170)),
        ((675, 170, 900, 360), "ScoreService\nvalidate", (41, 199, 142)),
        ((985, 170, 1210, 360), "ScoreDao\nINSERT ?", (27, 166, 79)),
        ((1295, 170, 1460, 360), "Oracle\ncommitted", (25, 48, 74)),
    ]
    for xy, label, fill in boxes:
        draw_box(draw, xy, label, fill, size=19)
    for left, right in zip(boxes, boxes[1:]):
        arrow(draw, (left[0][2], 265), (right[0][0], 265))
    draw.text((55, 420), "done() updates the visible status after success or failure; the EDT never performs the JDBC call.", fill=(20, 33, 48), font=font(18, False))
    image.save(FIGURES / "data_flow.png")


def make_erd():
    image = Image.new("RGB", (1400, 700), (255, 248, 224))
    draw = ImageDraw.Draw(image)
    draw.text((45, 25), "Campus Rush Entity Relationship Diagram", fill=(20, 33, 48), font=font(28, True))
    draw_box(draw, (100, 160, 560, 580), "PLAYERS\n\nPK player_id NUMBER\nUQ display_name VARCHAR2(40)\ncreated_at TIMESTAMP\nupdated_at TIMESTAMP", (25, 48, 74), size=21)
    draw_box(draw, (840, 160, 1300, 580), "SCORES\n\nPK score_id NUMBER\nFK player_id NUMBER\nscore_value NUMBER\ndistance_value NUMBER\nachieved_at TIMESTAMP", (27, 166, 79), size=21)
    arrow(draw, (560, 370), (840, 370))
    draw.text((625, 325), "1 to many", fill=(20, 33, 48), font=font(20, True))
    image.save(FIGURES / "erd.png")


def make_uml():
    image = Image.new("RGB", (1500, 760), (235, 255, 252))
    draw = ImageDraw.Draw(image)
    draw.text((45, 25), "Selected Class Relationships", fill=(20, 33, 48), font=font(28, True))
    draw_box(draw, (45, 130, 380, 620), "PlayerService\n\n+ createProfile\n+ updateProfile\n+ deleteProfile\n+ listProfiles", (107, 82, 170), size=20)
    draw_box(draw, (560, 130, 895, 620), "PlayerDao\n\n+ findAll\n+ create\n+ update\n+ delete\n- map", (27, 166, 79), size=20)
    draw_box(draw, (1075, 130, 1455, 620), "GameEngine\n\n+ jump\n+ setDucking\n+ tick\n+ snapshot\n- hasCollision", (239, 111, 89), size=20)
    arrow(draw, (380, 375), (560, 375))
    draw.text((425, 325), "uses", fill=(20, 33, 48), font=font(18, False))
    draw.text((940, 325), "independent", fill=(20, 33, 48), font=font(18, False))
    image.save(FIGURES / "uml.png")


def set_cell_shading(cell, fill):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = tc_pr.find(qn("w:shd"))
    if shd is None:
        shd = OxmlElement("w:shd")
        tc_pr.append(shd)
    shd.set(qn("w:fill"), fill)


def set_cell_border(cell, color="D9D9D9", size="6"):
    tc = cell._tc
    tc_pr = tc.get_or_add_tcPr()
    borders = tc_pr.first_child_found_in("w:tcBorders")
    if borders is None:
        borders = OxmlElement("w:tcBorders")
        tc_pr.append(borders)
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        tag = "w:" + edge
        element = borders.find(qn(tag))
        if element is None:
            element = OxmlElement(tag)
            borders.append(element)
        element.set(qn("w:val"), "single")
        element.set(qn("w:sz"), size)
        element.set(qn("w:space"), "0")
        element.set(qn("w:color"), color)


def format_table(table, header_fill="19304A"):
    table.style = "Table Grid"
    header_row = table.rows[0]._tr
    header_properties = header_row.get_or_add_trPr()
    repeat_header = OxmlElement("w:tblHeader")
    repeat_header.set(qn("w:val"), "true")
    header_properties.append(repeat_header)
    for row_index, row in enumerate(table.rows):
        for cell in row.cells:
            cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
            set_cell_border(cell)
            for paragraph in cell.paragraphs:
                paragraph.paragraph_format.space_after = Pt(2)
                for run in paragraph.runs:
                    run.font.name = "Aptos"
                    run.font.size = Pt(8.5)
            if row_index == 0:
                set_cell_shading(cell, header_fill)
                for paragraph in cell.paragraphs:
                    for run in paragraph.runs:
                        run.font.bold = True
                        run.font.color.rgb = RGBColor(255, 255, 255)
            elif row_index % 2 == 0:
                set_cell_shading(cell, "F2F7F8")


def add_table(doc, headers, rows, widths=None):
    table = doc.add_table(rows=1, cols=len(headers))
    for i, header in enumerate(headers):
        table.rows[0].cells[i].text = str(header)
    for row in rows:
        cells = table.add_row().cells
        row_properties = table.rows[-1]._tr.get_or_add_trPr()
        cant_split = OxmlElement("w:cantSplit")
        row_properties.append(cant_split)
        for i, value in enumerate(row):
            cell = cells[i]
            if isinstance(value, EvidenceImages):
                cell.text = ""
                added = False
                for path in value.paths:
                    paragraph = cell.paragraphs[0] if not added else cell.add_paragraph()
                    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
                    if path.exists():
                        paragraph.add_run().add_picture(str(path), width=Inches(value.width))
                        added = True
                if not added:
                    cell.text = "Evidence image unavailable"
            else:
                cell.text = str(value)
    if widths:
        for row in table.rows:
            for i, width in enumerate(widths):
                row.cells[i].width = Inches(width)
    format_table(table)
    doc.add_paragraph()
    return table


def add_heading(doc, text, level):
    paragraph = doc.add_heading(text, level=level)
    for run in paragraph.runs:
        run.font.color.rgb = RGBColor(0, 0, 0)
    return paragraph


def add_body(doc, text, bold_prefix=None):
    paragraph = doc.add_paragraph()
    paragraph.paragraph_format.space_after = Pt(6)
    paragraph.paragraph_format.line_spacing = 1.08
    if bold_prefix and text.startswith(bold_prefix):
        paragraph.add_run(bold_prefix).bold = True
        paragraph.add_run(text[len(bold_prefix):])
    else:
        paragraph.add_run(text)
    return paragraph


def add_caption(doc, text):
    paragraph = doc.add_paragraph()
    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = paragraph.add_run(text)
    run.italic = True
    run.font.size = Pt(9)
    return paragraph


def add_figure(doc, path, caption, width=6.8):
    paragraph = doc.add_paragraph()
    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    paragraph.paragraph_format.keep_with_next = True
    paragraph.add_run().add_picture(str(path), width=Inches(width))
    add_caption(doc, caption)


def evidence_path(name):
    for path in (EVIDENCE / name, DOCS / "evidence" / name):
        if path.exists():
            return path
    return EVIDENCE / name


def evidence_images(name, *additional_names, width=1.75):
    names = (name,) + additional_names
    return EvidenceImages(*(evidence_path(item) for item in names), width=width)


def add_optional_figure(doc, path, caption, width=6.8):
    if path.exists():
        add_figure(doc, path, caption, width)


def configure_document(doc):
    section = doc.sections[0]
    section.page_width = Inches(8.5)
    section.page_height = Inches(11)
    section.top_margin = Inches(0.65)
    section.bottom_margin = Inches(0.65)
    section.left_margin = Inches(0.7)
    section.right_margin = Inches(0.7)

    styles = doc.styles
    normal = styles["Normal"]
    normal.font.name = "Aptos"
    normal.font.size = Pt(10.5)
    normal._element.rPr.rFonts.set(qn("w:ascii"), "Aptos")
    normal._element.rPr.rFonts.set(qn("w:hAnsi"), "Aptos")
    for style_name, size in (("Title", 28), ("Heading 1", 18), ("Heading 2", 13)):
        style = styles[style_name]
        style.font.name = "Aptos Display"
        style.font.size = Pt(size)
        style.font.bold = True
        style.font.color.rgb = RGBColor(0, 0, 0)
        style._element.rPr.rFonts.set(qn("w:ascii"), "Aptos Display")
        style._element.rPr.rFonts.set(qn("w:hAnsi"), "Aptos Display")

    footer = section.footer
    p = footer.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(f"Campus Rush • Release {RELEASE}")
    r.font.size = Pt(8)
    r.font.color.rgb = RGBColor(90, 90, 90)


def create_document():
    make_architecture()
    make_data_flow()
    make_erd()
    make_uml()

    doc = Document()
    configure_document(doc)

    title = doc.add_paragraph(style="Title")
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    title.add_run("Campus Rush")
    subtitle = doc.add_paragraph()
    subtitle.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = subtitle.add_run("Final Project Documentation")
    run.font.size = Pt(16)
    run.bold = True
    run.font.color.rgb = RGBColor(25, 48, 74)
    for line in [
        "Students: Joshua Adam James Bebbington, Brandon Vladimir B. Cases",
        f"Section: {SECTION}",
        f"Release: {RELEASE}",
        "Database: Oracle Database 21c XE • localhost:1521/xepdb1",
        "Java: JDK 26.0.2 (source release 17)",
        f"Repository: {REPOSITORY}",
        "Submission date: 20 November 2026",
    ]:
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.add_run(line)
    doc.add_page_break()

    add_heading(doc, "Table of Contents", 1)
    for item in [
        "1 Cover and Project Information",
        "2 Project Overview",
        "3 Setup Guide",
        "4 User Guide",
        "5 Architecture and Design",
        "6 Database Design",
        "7 Business Rules and Reliability",
        "8 Test Report",
        "9 Traceability Matrix",
        "10 Milestone Evidence Index",
        "11 AI Use Log",
        "12 Reflection",
    ]:
        add_body(doc, item)
    doc.add_page_break()

    add_heading(doc, "1 Cover and Project Information", 1)
    add_body(doc, "Campus Rush is a Java Swing and JDBC desktop release for a 2D campus endless runner. This document describes the submitted source structure, database setup, user workflows, reliability rules, tests, traceability, and defense preparation.")
    add_table(doc, ["Item", "Value"], [
        ("Project", "Campus Rush"),
        ("Release", RELEASE),
        ("Student", STUDENTS),
        ("Section", SECTION),
        ("Database", "Oracle Database 21c XE; localhost:1521; service xepdb1"),
        ("Driver", "Oracle ojdbc11"),
        ("Java", "JDK 26.0.2; Maven source release 17"),
        ("Repository", REPOSITORY),
        ("Date", "20 November 2026"),
    ], [2.2, 4.5])

    add_heading(doc, "2 Project Overview", 1)
    add_body(doc, "Campus Rush is designed for students who want a short, replayable campus-themed arcade game. The runner jumps over ground obstacles such as desks, chairs, and students, and ducks under airborne equations and books while the score and speed increase. The release adds profile management, score persistence, searchable and sortable leaderboard reporting, and a clean restart workflow.")
    add_body(doc, "The core workflows are profile create/read/update/delete, play/pause/restart, score save on game over, leaderboard refresh/search/sort, and restart persistence. Out of scope are online multiplayer, user authentication, sound assets, cloud hosting, and platform-specific deployment installers.")

    add_heading(doc, "3 Setup Guide", 1)
    add_heading(doc, "3.1 Prerequisites", 2)
    add_body(doc, "Install JDK 17 or newer, Maven 3.9 or newer, and Oracle Database 21c Express Edition or the instructor-approved Oracle database. The verified connection is localhost:1521 with service xepdb1 and the SYSTEM schema. The Maven build resolves ojdbc11 from its declared dependency.")
    add_heading(doc, "3.2 Database setup", 2)
    add_body(doc, "Connect to the approved Oracle schema and run schema.sql once. Run seed_data.sql only when a sample leaderboard is desired. The schema creates PLAYERS and SCORES, enforces unique display names, checks nonnegative score values, indexes leaderboard access, and cascades scores when a profile is deleted.")
    add_heading(doc, "3.3 Configuration, build, and run", 2)
    add_body(doc, "Copy campus-rush.properties.example to campus-rush.properties and set the local password. The ignored local file keeps credentials out of source control. From the project directory run mvn clean compile, then mvn exec:java, or open the Maven project in NetBeans and run com.campusrush.App.")
    add_heading(doc, "3.4 Troubleshooting", 2)
    add_body(doc, "A missing driver means Maven or the NetBeans classpath has not resolved ojdbc11. ORA-00942 means schema.sql was run in a different Oracle service or schema. A database error in the UI should leave the application open and display an actionable status message; verify the URL, user, password, and xepdb1 service.")

    add_heading(doc, "4 User Guide", 1)
    add_heading(doc, "4.1 Select a profile", 2)
    add_body(doc, "Choose Profiles from the main menu. Create a name from 2 to 40 characters, select an existing profile to update or use, or delete a selected profile after confirming that its saved scores should also be removed. Profile operations use background workers so the window stays responsive.")
    add_heading(doc, "4.2 Play Campus Rush", 2)
    add_body(doc, "Choose Play after selecting a profile. Tap Space or Up for a short hop, or hold it longer for a higher jump. Hold Down to duck. Press P to pause and resume. Desks, chairs, and students occupy the ground path; equations and flying books occupy the overhead path. The speed increases as the score grows. A collision ends the run and saves the score in the background.")
    add_heading(doc, "4.3 Review the leaderboard", 2)
    add_body(doc, "Choose Leaderboard to refresh saved scores. Enter a player-name fragment and select Search / Refresh to filter the report. Click a table column to sort the visible rows. The report is read from Oracle each time it is refreshed.")
    add_heading(doc, "4.4 Evidence screenshots", 2)
    add_optional_figure(doc, evidence_path("figures/figure-01-main-menu.png"), "Figure 1. Campus Rush main menu with the selected player profile.", 6.8)
    add_optional_figure(doc, evidence_path("figures/figure-02-profiles.png"), "Figure 2. Profile management screen showing a saved profile.", 6.8)
    add_optional_figure(doc, evidence_path("figures/figure-03-active-game.png"), "Figure 3. Active game showing the player and a campus obstacle.", 6.8)
    add_optional_figure(doc, evidence_path("figures/figure-04-game-over.png"), "Figure 4. Game-over screen confirming that the score was saved.", 6.8)
    add_optional_figure(doc, evidence_path("figures/figure-05-leaderboard.png"), "Figure 5. Leaderboard filtered by the player name.", 6.8)

    add_heading(doc, "5 Architecture and Design", 1)
    add_body(doc, "The application is divided into presentation, game, service, model, DAO, configuration, and exception packages. UI listeners call services directly or through SwingWorker tasks; SQL is isolated inside DAOs. GameEngine is independent of Swing and JDBC, which makes movement and collision rules easier to test and explain during the defense.")
    add_figure(doc, FIGURES / "architecture.png", "Figure 6. Layered Campus Rush architecture.")
    add_figure(doc, FIGURES / "data_flow.png", "Figure 7. Score-save data flow from game over to Oracle and back to the view.")
    doc.add_page_break()
    add_figure(doc, FIGURES / "uml.png", "Figure 8. Selected class relationships in the submitted source.")
    add_heading(doc, "5.1 Design alternatives", 2)
    add_body(doc, "A monolithic JFrame with SQL inside button listeners would be shorter initially but would block the EDT, duplicate validation, and make defense tracing difficult. The release instead uses services and DAOs, SwingWorker for slow work, Java records for immutable values, and a pure game engine. Java2D shapes were chosen instead of external art assets so the release remains reproducible and every visual element is explainable from source.")

    add_heading(doc, "6 Database Design", 1)
    add_body(doc, "Players represent selectable game profiles. Scores represent completed runs and reference the player who achieved them. A player may have many scores; deleting a player cascades the associated scores. Unique display names prevent ambiguous profile selection, and check constraints reject invalid negative values at the database boundary.")
    add_figure(doc, FIGURES / "erd.png", "Figure 9. PLAYERS to SCORES entity relationship diagram.")
    add_table(doc, ["Table", "Key fields", "Integrity rules"], [
        ("PLAYERS", "player_id PK; display_name UQ", "2-40 character name; timestamps required"),
        ("SCORES", "score_id PK; player_id FK", "score and distance nonnegative; cascade on player delete"),
    ], [1.3, 2.0, 3.4])

    add_heading(doc, "7 Business Rules and Reliability", 1)
    add_table(doc, ["Rule", "Enforcement location"], [
        ("Profile names must be 2-40 characters.", "PlayerService.validName and PLAYERS check constraint"),
        ("Profile names are unique.", "PLAYERS unique constraint and PlayerDao duplicate handling"),
        ("A score requires a valid player and nonnegative values.", "ScoreService.saveScore and SCORES constraints"),
        ("Ground obstacles require a jump; airborne obstacles can be cleared by ducking.", "GameEngine obstacle types and hasCollision"),
        ("A short jump tap is lower than a longer jump hold.", "GamePanel jump bindings and GameEngine.jump/releaseJump"),
        ("Speed increases as the score increases.", "GameEngine.tick"),
        ("Database work must not block the Swing EDT.", "SwingWorker in ProfilePanel, LeaderboardPanel, and GamePanel"),
        ("Visible status follows the committed database result.", "SwingWorker.done and DAO affected-row checks"),
    ], [3.2, 3.5])
    add_body(doc, "All DAO connections, prepared statements, and result sets use try-with-resources. SQL values are bound with question-mark parameters. Data-access failures become DataAccessException messages at the service/UI boundary, while validation failures are shown before a database call. A failed score save does not close the game window or silently claim success.")

    doc.add_page_break()
    add_heading(doc, "8 Test Report", 1)
    add_body(doc, "The following 18 tests cover the required feature set and the edge cases named in the specification.")
    test_rows = [
        ("T01", "Clean schema setup", "Tables and constraints created", "Pass | DBeaver showed the PLAYERS and SCORES tables.", evidence_images("tests/t01-schema.png")),
        ("T02", "Create profile", "One profile appears", "Pass | The profile was saved and appeared in the profile list.", evidence_images("tests/t02-create-profile.png")),
        ("T03", "Duplicate profile", "Duplicate message; no row", "Pass | The duplicate-name message appeared and no second profile was created.", evidence_images("tests/t03-duplicate-profile.png")),
        ("T04", "One-character name", "Validation message", "Pass | The one-character name was rejected with a validation message.", evidence_images("tests/t04-invalid-name.png")),
        ("T05", "Apostrophe in name", "Saved safely", "Pass | The apostrophe name was saved successfully.", evidence_images("tests/t05-apostrophe-name.png")),
        ("T06", "Update profile", "Changed name appears", "Pass | The updated profile name appeared in the list.", evidence_images("tests/t06-update-profile.png")),
        ("T07", "Delete profile", "Profile and scores removed", "Pass | The profile and its saved scores were removed.", evidence_images("tests/t07-delete-profile-1.png", "tests/t07-delete-profile-2.png")),
        ("T08", "Ground collision", "Game over and save starts", "Pass | A ground collision caused game over.", evidence_images("tests/t08-ground-collision.png")),
        ("T09", "Duck overhead obstacle", "Correct duck clears obstacle", "Pass | The player ducked under the overhead obstacle successfully.", evidence_images("tests/t09-duck-obstacle.png")),
        ("T10", "Pause/resume", "Timer stops and resumes", "Pass | Pressing P paused the game and pressing P again resumed it.", evidence_images("tests/t10-pause.png", "tests/t10-resume.png")),
        ("T11", "Save completed score", "One score committed", "Pass | The game displayed the saved-score message.", evidence_images("tests/t11-score-saved.png")),
        ("T12", "Restart persistence", "Score remains after restart", "Pass | The score remained in the leaderboard after restarting the application.", evidence_images("tests/t12-restart-persistence.png")),
        ("T13", "Leaderboard search", "Name fragment filters", "Pass | Searching a name fragment showed only matching results.", evidence_images("tests/t13-search.png")),
        ("T14", "Leaderboard sort", "Column sort changes order", "Pass | Clicking a leaderboard column changed the row order.", evidence_images("tests/t14-sort.png")),
        ("T15", "Database outage", "Actionable error; UI usable", "Pass | A database error appeared while the application remained usable.", evidence_images("tests/t15-database-outage.png")),
        ("T16", "Repeated refresh", "No duplicate view rows", "Pass | Repeated refreshes did not duplicate leaderboard rows.", evidence_images("tests/t16-refresh-1.png", "tests/t16-refresh-2.png")),
        ("T17", "Blank search", "Full leaderboard returns", "Pass | A blank search returned the full leaderboard.", evidence_images("tests/t17-blank-search.png")),
        ("T18", "Variable jump height", "Short tap is lower than a longer hold", "Pass | Holding the jump key longer produced a higher jump than a quick tap.", evidence_images("tests/t18-short-tap.png", "tests/t18-long-hold.png")),
    ]
    add_table(doc, ["ID", "Scenario", "Expected", "Actual", "Evidence"], test_rows, [0.45, 1.45, 1.7, 1.55, 1.95])
    add_body(doc, "Pass/fail summary: complete after local execution. Known environment dependencies are the Oracle service, the driver, the local password, and the availability of the instructor-approved schema. No test should be marked passed without an observed result and evidence reference.")

    doc.add_page_break()
    add_heading(doc, "9 Traceability Matrix", 1)
    add_body(doc, "The group was not provided separate technical summative documents for this project. Therefore, the traceability matrix maps the final Campus Rush features directly to their source classes, methods, tests, and available evidence.")

    add_heading(doc, "10 Milestone Evidence Index", 1)
    add_body(doc, "The group was not provided separate TSA1–TSA4 technical summative folders, files, or approved repository tags. The final source code, database scripts, documentation, screenshots, and test results are therefore used as the available project evidence.")

    doc.add_page_break()
    add_heading(doc, "11 AI Use Log", 1)
    add_body(doc, "AI assistance was used as a development aid for an initial architecture/code draft, campus obstacle ideas, reliability review, test planning, and documentation structure. The student remains responsible for understanding, modifying, testing, and explaining every submitted part; actual local results must be recorded after running the release.")
    add_table(doc, ["Checkpoint", "AI support", "Student verification/change"], [
        ("1", "Suggested a layered Java Swing/JDBC structure and an initial project scaffold.", "Reviewed package responsibilities, kept SQL inside DAOs, and must verify the clean build and run."),
        ("2", "Suggested campus obstacle categories and jump, duck, pause, and collision behavior.", "Adjusted game rules and must test controls and collision cases in the submitted release."),
        ("3", "Reviewed validation, database failure handling, test coverage, and documentation organization.", "Must run acceptance tests, replace evidence placeholders, and make final corrections."),
    ], [0.65, 2.5, 3.35])

    add_heading(doc, "12 Reflection", 1)
    add_body(doc, "The most important redesign was separating the game engine, services, DAOs, and Swing panels so each responsibility can be traced during the live defense. This made it possible to keep database work off the EDT, keep credentials out of source code, and reuse the same profile and score rules across the interface and database. The evidence-based improvement I would make next is to add automated headless tests for GameEngine collision cases and run the complete acceptance suite in continuous integration after every change.")

    doc.save(OUTPUT)


if __name__ == "__main__":
    create_document()
    print(OUTPUT)
