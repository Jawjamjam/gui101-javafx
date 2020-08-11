package gui101;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class Controller implements Initializable {
    private float yOffset, xOffset;

    private Stage stage;

    @FXML
    private AnchorPane pnlTool;

    @FXML
    private Button btnMinimize, btnMaximize, btnExit, btnSave, btnNew, btnDelete;

    @FXML
    private HTMLEditor edText;
    
    @FXML
    private ListView<NoteData> lvNotes;

    private String selectedNoteID;

    private ObservableList<NoteData> notes;

    /**
     * Initializes the object instance with default values for the non-fxml
     * attributes.
     */
    public Controller() {
         this.setNotes(FXCollections.observableArrayList());
         this.setSelectedNoteID("");
    }

    /**
     * This methods runs after all FXML components have been loaded to memory.
     * Manipulates the various properties in the FXML components.
     * Binds the notes attribute to the lvNotes (ListView) FXML component.
     * Adjusts the cell rendering/factory for lvNotes to display NodeHead data.
     * Sets the different actions to perform when clicking btnSave (Save), btnNew (New Note),
     * btnDelete (Delete Note).
     * Adds Key Listeners to the edText (HTMLEditor) to allow for keyboard shortcuts (CTRL+S - Save),
     * to dictate when to enable the save button (btnSave), and to adjust the strSample in
     * the selected NoteData.
     * @param location - The location used to resolve relative paths for the root object,
     *                 or null if the location is not known.
     * @param resources - The resources used to localize the root object, or null if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
       this.lvNotes.setItems(this.getNotes());
       this.lvNotes.setCellFactory(param -> new NoteCell());

        this.btnSave.setOnAction(v -> this.save());

        this.lvNotes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.lvNotes.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (this.getNotes().size() > 0) {
                this.setSelectedNoteID(newValue.getStrID());

                this.loadNote(this.getSelectedNoteID());
            } else {
                this.disableEditor();
                this.clearEditor();
            }
        }));

        this.edText.setOnKeyPressed(v -> {
            this.enableSave();

            if (v.isControlDown() && v.getCode() == KeyCode.S)
                this.save();
        });

        this.edText.setOnKeyReleased(v -> {
            this.lvNotes.getSelectionModel().getSelectedItem().setStrSample(String.format("%.20s", this.edText.getHtmlText().replaceAll("</p>", "</p>\n").replaceAll("<[^>]*>", "").replaceAll("&[^;]*;", " ")));
            this.lvNotes.refresh();
        });
        
        this.btnNew.setOnAction(v -> {
            this.enableSave();
            this.enableEditor();
            this.enableDelete();

            NoteData note = new NoteData();
            this.getNotes().add(note);

            this.newNote(note.getStrID());

            this.lvNotes.getSelectionModel().selectLast();
        });

        this.btnDelete.setOnAction(v -> {
            this.disableSave();
            this.disableEditor();
            this.disableDelete();

            this.delete();
        });

        this.loadNotes();
    }

    /**
     * Sets the stage attribute for the instance.
     * Also binds actions that require connection to the stage/window which includes:
     * Exiting (btnExit) the window, Maximizing (btnMaximize) the window, Minimizing (btnMinimize) the window,
     * and Moving (pnlTool) the window.
     * @param stage - the Stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;

        this.btnExit.setOnAction(v -> Platform.runLater(() -> {
            this.stage.close();
            Platform.exit();
        }));

        this.btnMinimize.setOnAction(v -> this.stage.setMaximized(false));

        this.btnMaximize.setOnAction(v -> this.stage.setMaximized(true));

        this.pnlTool.setOnMousePressed(v -> {
            this.setXOffset((float) (this.getStage().getX() - v.getScreenX()));
            this.setYOffset((float) (this.getStage().getY() - v.getScreenY()));
        });

        this.pnlTool.setOnMouseDragged(v -> {
            this.getStage().setX(v.getScreenX() + this.getXOffset());
            this.getStage().setY(v.getScreenY() + this.getYOffset());
        });
    }

    /**
     * Sets the X Offset (How much the stage/window moved horizontally).
     * @param xOffset - the horizontal offset from the origin
     */
    public void setXOffset(float xOffset) {
        this.xOffset = xOffset;
    }

    /**
     * Sets the Y Offset (How much the stage/window moved vertically).
     * @param yOffset - the vertical offset from the origin
     */
    public void setYOffset(float yOffset) {
        this.yOffset = yOffset;
    }

    /**
     * Sets the selected note ID to load/manipulate
     * @param selectedNoteID - the selected note ID
     */
    public void setSelectedNoteID(String selectedNoteID) {
        this.selectedNoteID = selectedNoteID;
    }

    /**
     * Sets the notes observable list
     * @param notes - the notes list
     */
    public void setNotes(ObservableList<NoteData> notes) {
        this.notes = notes;
    }

    /**
     * @return returns the stage/window assigned to the object
     */
    public Stage getStage() {
        return this.stage;
    }

    /**
     * @return returns the horizontal offset assigned to the object
     */
    public float getXOffset() {
        return this.xOffset;
    }

    /**
     * @return returns the vertical offset assigned to the object
     */
    public float getYOffset() {
        return this.yOffset;
    }

    /**
     * @return returns the selected note ID assigned to the object
     */
    public String getSelectedNoteID() {
        return this.selectedNoteID;
    }

    /**
     * @return returns the notes list assigned to the object
     */
    public ObservableList<NoteData> getNotes() {
        return this.notes;
    }

    /**
     * Enables the save button (btnSave)
     */
    private void enableSave() {
        this.btnSave.setOpacity(1);
        this.btnSave.setDisable(false);
    }

    /**
     * Enables the editor (edText)
     */
    private void enableEditor() {
        this.edText.setDisable(false);
    }

    /**
     * Enables the delete button (btnDelete)
     */
    public void enableDelete() {
        this.btnDelete.setDisable(false);
    }

    /**
     * Disables the save button (btnSave)
     */
    private void disableSave() {
        this.btnSave.setOpacity(0);
        this.btnSave.setDisable(true);
    }

    /**
     * Disables the editor (edText)
     */
    private void disableEditor() {
        this.edText.setDisable(true);
    }

    /**
     * Disables the delete button (btnDelete)
     */
    public void disableDelete() {
        this.btnDelete.setDisable(true);
    }

    /**
     * Clears the editor (edText)
     */
    public void clearEditor() {
        this.edText.setHtmlText("");
    }

    /**
     * Saves the data into a specific file.
     * The filename format is: <the selected note id>.note
     * The file is also saved in the sub directory "notes"
     */
    private void save() {
         Path path = Paths.get("notes/" + this.getSelectedNoteID() + ".note");
        try {
            String temp = this.edText.getHtmlText().replaceAll("</p>", "</p>\n");
            Files.write(path, temp.getBytes());
            this.disableSave();
        } catch (IOException e) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Failed to Save");
            a.setHeaderText("Uh Oh!");
            a.setContentText("I don't think we can save this note right now, try again later.");
            a.showAndWait();
        }
    }

    /**
     * Deletes the file associated with the selected note ID.
     * The filename format is: <the selected note id>.note
     * The file deleted is from the sub directory "notes"
     */
    private void delete() {
        Path path = Paths.get("notes/" + this.getSelectedNoteID() + ".note");
        try {
            Files.delete(path);
            
            for (int i = 0; i < this.getNotes().size(); i++) {
                if (this.getNotes().get(i).getStrID().equals(this.getSelectedNoteID())) {
                    this.getNotes().remove(i);
                    break;
                }
            }
        } catch (IOException e) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Failed to Delete");
            a.setHeaderText("Wenkwonk!");
            a.setContentText("We can't delete this note right now, try again later.");
            a.showAndWait();
        }
    }

    /**
     * Loads the note from a specific file.
     * The note data is loaded into the editor (edText)
     * @param noteID - the note ID associated with a specific file.
     */
    public void loadNote(String noteID) {
        Path path = Paths.get("notes/" + noteID + ".note");
        String line;
        try {
            this.enableDelete();
            BufferedReader bufferedReader = Files.newBufferedReader(path);
            this.clearEditor();

            while((line = bufferedReader.readLine()) != null) {
                this.edText.setHtmlText(this.edText.getHtmlText() + line);
            }

            this.enableEditor();
        } catch (IOException e) {
            this.disableDelete();
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Failed to Load");
            a.setHeaderText("Oop!");
            a.setContentText("We can't load the note right now, it might've been deleted or moved.");
            a.showAndWait();
        }
    }

    /**
     * Creates a new note file under the "notes" subdirectory.
     * The filename format is: <the note id>.note
     * @param noteID - the note id/filename to use in creating the file
     */
    private void newNote(String noteID) {
        Path path = Paths.get("notes/" + noteID + ".note");
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, "".getBytes());
        } catch (IOException e) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Failed to Create");
            a.setHeaderText("Huh!");
            a.setContentText("We can't create the note file right now, make sure you've got the right privileges.");
            a.showAndWait();
        }
    }

    /**
     * Lists all notes within the "notes" subdirectory and places them in a NoteHead
     * inside the notes list (lvNotes).
     * The note title/sample is read from 20 characters within the first line of each file.
     */
    public void loadNotes() {
        Path path = Paths.get("notes/");
        StringBuilder builder = new StringBuilder();
        try {
            Stream<Path> list = Files.list(path);
            list.filter(p -> !Files.isDirectory(p) && p.toString().lastIndexOf(".note") != -1)
                .forEach(p -> {
                    String strID = p.toString().substring(6, p.toString().lastIndexOf(".note"));
                    NoteData note = new NoteData();
                    note.setStrID(strID);
                    BasicFileAttributes attr;
                    String line;

                    try {
                        BufferedReader bufferedReader = Files.newBufferedReader(p);

                        if((line = bufferedReader.readLine()) != null) {
                            note.setStrSample(String.format("%.20s", line.replaceAll("</p>", "</p>\n").replaceAll("<[^>]*>", "")));
                        } else {
                            note.setStrSample(" ");
                        }
                        bufferedReader.close();

                        attr = Files.readAttributes(p, BasicFileAttributes.class);
                        note.setDate(attr.creationTime().toMillis());

                        getNotes().add(note);
                    } catch (IOException e) {
                        builder.append(p).append("\n");
                    }
                });

            if (builder.length() > 0) {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Failed to Load a Note");
                a.setHeaderText("Oop!");
                a.setContentText("We can't load these notes right now:\n" + builder.toString());
                a.showAndWait();
            }
        } catch (IOException e) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Failed to List Notes");
            a.setHeaderText("Uhh!");
            a.setContentText("It seems like we can't list your notes right now.");
            a.showAndWait();
        }
    }
}
