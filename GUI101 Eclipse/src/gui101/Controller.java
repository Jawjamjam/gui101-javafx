package gui101;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    private AnchorPane pnlTool, pnlBody;

    @FXML
    private Button btnMinimize, btnMaximize, btnExit, btnSave, btnNew, btnDelete;

    @FXML
    private HTMLEditor edText;
    
    @FXML
    private ListView<NoteData> lvNotes;

    private String selectedNoteID;

    private ObservableList<NoteData> notes;

    public Controller() {
         notes = FXCollections.observableArrayList();
    }
    
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

    public void setStage(Stage stage) {
        this.stage = stage;

        this.btnExit.setOnAction(v -> Platform.runLater(() -> {
            this.stage.close();
            Platform.exit();
        }));

        this.btnMinimize.setOnAction(v -> this.stage.setMaximized(false));

        this.btnMaximize.setOnAction(v -> this.stage.setMaximized(true));

        this.pnlTool.setOnMousePressed(v -> {
            this.setxOffset((float) (this.getStage().getX() - v.getScreenX()));
            this.setyOffset((float) (this.getStage().getY() - v.getScreenY()));
        });

        this.pnlTool.setOnMouseDragged(v -> {
            this.getStage().setX(v.getScreenX() + this.getxOffset());
            this.getStage().setY(v.getScreenY() + this.getyOffset());
        });
    }

    public void setxOffset(float xOffset) {
        this.xOffset = xOffset;
    }

    public void setyOffset(float yOffset) {
        this.yOffset = yOffset;
    }

    public void setSelectedNoteID(String selectedNoteID) {
        this.selectedNoteID = selectedNoteID;
    }

    public void setNotes(ObservableList<NoteData> notes) {
        this.notes = notes;
    }

    public Stage getStage() {
        return this.stage;
    }

    public float getxOffset() {
        return this.xOffset;
    }

    public float getyOffset() {
        return this.yOffset;
    }

    public String getSelectedNoteID() {
        return this.selectedNoteID;
    }

    public ObservableList<NoteData> getNotes() {
        return this.notes;
    }

    private void enableSave() {
        this.btnSave.setOpacity(1);
        this.btnSave.setDisable(false);
    }

    private void enableEditor() {
        this.edText.setDisable(false);
    }

    public void enableDelete() {
        this.btnDelete.setDisable(false);
    }

    private void disableSave() {
        this.btnSave.setOpacity(0);
        this.btnSave.setDisable(true);
    }

    private void disableEditor() {
        this.edText.setDisable(true);
    }

    public void disableDelete() {
        this.btnDelete.setDisable(true);
    }

    public void clearEditor() {
        this.edText.setHtmlText("");
    }

    private void save() {
         Path path = Paths.get("notes/" + this.getSelectedNoteID() + ".note");
        try {
            String temp = this.edText.getHtmlText().replaceAll("</p>", "</p>\n");
            Files.write(path, temp.getBytes());
            this.disableSave();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
    }

    private void newNote(String noteID) {
        Path path = Paths.get("notes/" + noteID + ".note");
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, "".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadNotes() {
        Path path = Paths.get("notes/");
        try {
            Stream<Path> list = Files.list(path);
            list.filter(p -> {
                if (!Files.isDirectory(p) && p.toString().lastIndexOf(".note") != -1) return true;
                return false;
            }).forEach(p -> {
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
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
