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
import java.util.concurrent.atomic.AtomicBoolean;
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
       this.lvNotes.setItems(this.getNotes()); // assign the list of NoteData to the notes ListView

        // change the default lvNotes' cell factory with the custom made NoteCell class
       this.lvNotes.setCellFactory(param -> new NoteCell());

        // add an event listener that calls the save function whenever btnSave is clicked
        this.btnSave.setOnAction(v -> this.save());

        // update the default settings of lvNotes to make sure you can only select one cell
        this.lvNotes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // add an event listener whenever a cell in lvNotes is clicked
        this.lvNotes.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            /*
                make sure there is still data inside the notes variable,
                this is to assure the program that there is something to display,
                when a specific cell in the list is selected
             */
            if (this.getNotes().size() > 0) {
                // change the selectedNoteID variable with the noteID in the selected cell in the list
                this.setSelectedNoteID(newValue.getStrID());

                // load the note onto the editor with the new selectedNoteID
                this.loadNote(this.getSelectedNoteID());
            } else {

                /*
                    if there is no data in the list, then an error has occurred,
                    and nothing should be displayed in the editor.
                 */
                this.disableEditor();
                this.clearEditor();
            }
        }));

        // add an event listener to the editor whenever the user presses a key while focusing on the editor
        this.edText.setOnKeyPressed(v -> {
            this.enableSave(); // this makes sure that whatever key the users pressed, the save button is enabled

            if (v.isControlDown() && v.getCode() == KeyCode.S) // check if the user press CTRL+S
                this.save(); // automatically save the note
        });

        // add an event listener to the editor whenever the user lifts a key after pressing it
        this.edText.setOnKeyReleased(v -> {
            /**
             * this changes the note sample whenever the user changes the first 20 characters in the editor,
             * after updating the note sample, refresh the notes list to update its GUI
             */
            this.lvNotes.getSelectionModel().getSelectedItem().setStrSample(String.format("%.20s", this.edText.getHtmlText().replaceAll("</p>", "</p>\n").replaceAll("<[^>]*>", "").replaceAll("&[^;]*;", " ")));
            this.lvNotes.refresh();
        });

        // add an event listener whenever the user clicks on the btnNew button
        this.btnNew.setOnAction(v -> {
            this.enableSave(); // enables the save button
            this.enableEditor(); // enables the editor
            this.enableDelete(); // enables the delete button

            NoteData note = new NoteData(); // instantiate a new NoteData object
            this.getNotes().add(note); // add the new NoteData object to the notes variable

            // call the newNote function with the unique ID generated by the new NoteData object,
            // and create the necessary file
            this.newNote(note.getStrID());

            // select the newest note in the list to load it into the editor
            this.lvNotes.getSelectionModel().selectLast();
        });

        // adds an event listener whenever the btnDelete button is clicked
        this.btnDelete.setOnAction(v -> {
            this.disableSave(); // disables the save button
            this.disableEditor(); // disables the editor
            this.disableDelete(); // disables the delete button

            // call the delete function and delete the file with the same name as the selectedNoteID
            this.delete();
        });

        // load the pre-existing notes inside the 'notes' subdirectory,
        // and add each note file into the note list
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
        /**
         * The codes below are in the setStage function, because on initialization(void initialize()) of the program
         * the stage would not yet be set, and these codes require access to the stage.
         */

        /**
         * add an event listener to whenever the user clicks the btnExit button.
         * this code makes sure that all actions currently going on in the program
         * (e.g. creating a new file, delete a file, updating a file), are over before the program exits
         */
        this.btnExit.setOnAction(v -> Platform.runLater(() -> {
            this.stage.close(); // close the program window
            Platform.exit(); // make sure the program fully closed by exiting the executable
        }));

        // add an event listener to whenever the user clicks the btnMinimize button,
        // this sets the maximized attribute of the stage to false
        this.btnMinimize.setOnAction(v -> this.stage.setMaximized(false));

        // add an event listener to whenever the user clicks the btnMaximize button,
        // this sets the maximized attribute of the stage to true
        this.btnMaximize.setOnAction(v -> this.stage.setMaximized(true));

        /*
            add an event listener to whenever the user clicks the toolbar section of the window
            we do this because in the Main class we disabled the default window toolbar in place of
            our custom one, which we can design however we want
         */
        this.pnlTool.setOnMousePressed(v -> {
            /**
             * after clicking the toolbar, update the x and y offset variables to get the cursors
             * position in the screen
             */
            this.setXOffset((float) (this.getStage().getX() - v.getScreenX()));
            this.setYOffset((float) (this.getStage().getY() - v.getScreenY()));
        });

        // add ane vent listener to whenever the user drags the toolbar section of the window
        this.pnlTool.setOnMouseDragged(v -> {
            /**
             * after dragging the toolbar, update the stage's x and y position in the screen to move
             * the entire window
             */
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
        // create a Path variable that leads to the notes file with the current selectedNoteID
        // that the user plans to update
        Path path = Paths.get("notes/" + this.getSelectedNoteID() + ".note");

        /**
         * Put the following code in a try-catch statement in case the path that was created does not exist
         * (the file does not exist)
         */
        try {
            /**
             * assign the texts in the editor to a temporary variable,
             * since the texts inside the editor is actually written in the HTML (see https://www.w3schools.com/html/)
             * language and then rendered in the editor GUI, we have to do some pre-processing on the data in order to
             * make the note file more readable.
             */
            String temp = this.edText.getHtmlText().replaceAll("</p>", "</p>\n");

            // save the texts in the temp variable to the path/file the program has created
            Files.write(path, temp.getBytes());

            // disable the save button
            this.disableSave();
        } catch (IOException e) {
            // if the file does not exist or some other error occurred, show an alert window
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Failed to Save");
            a.setHeaderText("Uh Oh!");
            a.setContentText("I don't think we can save this note right now, try again later.");

            // show the alert window but make the program window wait for the users response before enabling
            // the main program window again
            a.showAndWait();
        }
    }

    /**
     * Deletes the file associated with the selected note ID.
     * The filename format is: <the selected note id>.note
     * The file deleted is from the sub directory "notes"
     */
    private void delete() {
        // create a Path variable that leads to the notes file with the current selectedNoteID
        // that the user plans to delete
        Path path = Paths.get("notes/" + this.getSelectedNoteID() + ".note");
        try {
            // delete the path/file
            Files.delete(path);

            // update the notes list by removing the note that the user wants to delete
            for (int i = 0; i < this.getNotes().size(); i++) {
                // look for the NoteData with a similar noteID to the selectedNoteID
                if (this.getNotes().get(i).getStrID().equals(this.getSelectedNoteID())) {
                    this.getNotes().remove(i); // remove the NoteCell with the selectedNoteID from the notes data
                    this.lvNotes.refresh(); // refresh the notes list
                    break; // exit the loop after finding the specified note
                }
            }
        } catch (IOException e) {
            // if the file does not exist or some other error occurred, show an alert window
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Failed to Delete");
            a.setHeaderText("Wenkwonk!");
            a.setContentText("We can't delete this note right now, try again later.");

            // show the alert window but make the program window wait for the users response before enabling
            // the main program window again
            a.showAndWait();
        }
    }

    /**
     * Loads the note from a specific file.
     * The note data is loaded into the editor (edText)
     * @param noteID - the note ID associated with a specific file.
     */
    public void loadNote(String noteID) {
        // create a Path variable that leads to the notes file with the current selectedNoteID
        // that the user plans to load
        Path path = Paths.get("notes/" + noteID + ".note");
        String line; // initialize a variable that will contain the read lines in the note
        try {
            this.enableDelete(); // enable the delete button
            // instantiate a BufferedReader that will read the note file (given by the path variable) line by line
            BufferedReader bufferedReader = Files.newBufferedReader(path);

            // clear the editor of its contents
            this.clearEditor();

            /*
                while there are still lines we can read in the file, keep reading line after line and append each line
                into the editor
             */
            while((line = bufferedReader.readLine()) != null) {
                this.edText.setHtmlText(this.edText.getHtmlText() + line); // append the read line into the editor
            }

            // close the reader
            bufferedReader.close();
            // enable the editor
            this.enableEditor();
        } catch (IOException e) {
            // disable the delete button
            this.disableDelete();

            // if the file does not exist or some other error occurred, show an alert window
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Failed to Load");
            a.setHeaderText("Oop!");
            a.setContentText("We can't load the note right now, it might've been deleted or moved.");

            // show the alert window but make the program window wait for the users response before enabling
            // the main program window again
            a.showAndWait();
        }
    }

    /**
     * Creates a new note file under the "notes" subdirectory.
     * The filename format is: <the note id>.note
     * @param noteID - the note id/filename to use in creating the file
     */
    private void newNote(String noteID) {
        // create a Path variable that leads to the notes file with the noteID
        // that the user created
        Path path = Paths.get("notes/" + noteID + ".note");
        
        try {
            // create the necessary directories that the note file will be under
            Files.createDirectories(path.getParent());

            // create the note file and add empty content into it
            Files.write(path, "".getBytes());
        } catch (IOException e) {
            // if the directory or file cannot be created, or some other error occurred, show an alert window
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Failed to Create");
            a.setHeaderText("Huh!");
            a.setContentText("We can't create the note file right now, make sure you've got the right privileges.");

            // show the alert window but make the program window wait for the users response before enabling
            // the main program window again
            a.showAndWait();
        }
    }

    /**
     * Lists all notes within the "notes" subdirectory and places them in a NoteHead
     * inside the notes list (lvNotes).
     * The note title/sample is read from 20 characters within the first line of each file.
     */
    public void loadNotes() {
        // create a Path variable that leads to the directory that contains the notes files
        Path path = Paths.get("notes/");

        // create a builder that will contain the files that encountered an error while reading them
        StringBuilder builder = new StringBuilder();
        try {
            // create a stream of Paths that lead to each individual file in the "notes" directory
            Stream<Path> list = Files.list(path);

            // filter each path to make sure that the path only leads to '.note' files
            list.filter(p -> !Files.isDirectory(p) && p.toString().lastIndexOf(".note") != -1)
                .forEach(p -> { // loop through each file that were kept in the list
                    // get the noteID that the new NoteData will have, this noteID can be extracted from the file name
                    String strID = p.toString().substring(6, p.toString().lastIndexOf(".note"));

                    // instantiate a default NoteData object
                    NoteData note = new NoteData();
                    note.setStrID(strID); // set the noteID extracted from the file name to the new NoteData object

                    // create a BasicFileAttributes variable in order to extract metadata from the file
                    BasicFileAttributes attr;
                    String line;  // initialize a variable that will contain the read lines in the note

                    try {
                        // instantiate a BufferedReader that will read the note file (given by the path variable) line by line
                        BufferedReader bufferedReader = Files.newBufferedReader(p);

                        // read the first line in the note file, to extract the note sample
                        if((line = bufferedReader.readLine()) != null) {
                            /*
                            since the texts in the note file are actually in the HTML language, we remove the html components
                            in the first 20 characters in order to extract the true note sample
                             */
                            note.setStrSample(String.format("%.20s", line.replaceAll("</p>", "</p>\n").replaceAll("<[^>]*>", "")));
                        } else {
                            // if the file contains nothing (e.g. a newly created note), we give a blank note sample
                            note.setStrSample(" ");
                        }

                        // close the BufferedReader to allow other programs to access the file
                        bufferedReader.close();

                        // get the metadata of the file
                        attr = Files.readAttributes(p, BasicFileAttributes.class);

                        // set the date in the new NoteData with the creation time metadata of the file
                        note.setDate(attr.creationTime().toMillis());

                        // add the new NoteData to the notes variable which will then show up in the notes list
                        this.getNotes().add(note);
                    } catch (IOException e) {
                        // if an error occurred while reading the file, append the file name onto the builder variable
                        builder.append(p).append("\n");
                    }
                });

            if (builder.length() > 0) {
                // if there are files in the directory but all of them cannot be read,
                // or some other error occurred, show an alert window
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Failed to Load a Note");
                a.setHeaderText("Oop!");
                a.setContentText("We can't load these notes right now:\n" + builder.toString());

                // show the alert window but make the program window wait for the users response before enabling
                // the main program window again
                a.showAndWait();
            }
        } catch (IOException e) {
            // if the directory cannot be read, or some other error occurred, show an alert window
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Failed to List Notes");
            a.setHeaderText("Uhh!");
            a.setContentText("It seems like we can't list your notes right now.");

            // show the alert window but make the program window wait for the users response before enabling
            // the main program window again
            a.showAndWait();
        }
    }
}
