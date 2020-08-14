package gui101;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Date;

public class NoteCell extends ListCell<NoteData> {
    private AnchorPane pnlCell;
    private Label lblSample, lblDate;

    /**
     * Initializes Note data with default values for each attribute and initializes
     * the positioning and specific styles for the components in the list cell.
     */
    public NoteCell() {
        super();

        // instantiate the Labels that will be displayed (note sample label and date label)
        this.setLblSample(new Label());
        this.setLblDate(new Label());

        // update the note sample label with the parameters below
        this.getLblSample().setFont(Font.font("System", FontWeight.BOLD, 14)); // update the text style

        // update the note sample label location in the cell
        this.getLblSample().setLayoutX(10.0);
        this.getLblSample().setLayoutY(15.0);

        // update the width and height of the label container in the cell
        this.getLblSample().setPrefSize(115.0, 20.0);
        this.getLblSample().setMinSize(115.0, 20.0);
        this.getLblSample().setMaxSize(115.0, 20.0);

        // update the date label with the parameters below
        this.getLblDate().setFont(new Font("System", 10)); // update the text style

        // update the note sample label location in the cell
        this.getLblDate().setLayoutX(125.0);
        this.getLblDate().setLayoutY(13.0);

        // update the width and height of the label container in the cell
        this.getLblDate().setPrefSize(63.0, 20.0);
        this.getLblDate().setMinSize(63.0, 20.0);
        this.getLblDate().setMaxSize(63.0, 20.0);

        // update the cells main contain with an AnchorPane that contains the note sample and date labels
        this.setPnlCell(new AnchorPane(this.getLblDate(), this.getLblSample()));

        // update the width and height of the AnchorPane in the cell
        this.getPnlCell().setPrefSize(160.0, 50.0);
        this.getPnlCell().setMinSize(160.0, 50.0);
        this.getPnlCell().setMaxSize(160.0, 50.0);
    }

    /**
     * Overrides the default list cell item renderer, instead of displaying a text in the cell,
     * this override causes the main AnchorPane (pnlCell) to be displayed.
     * @param item - contains the data that should be displayed in the cell
     * @param empty - dictates whether the data is empty
     */
    @Override
    protected void updateItem(NoteData item, boolean empty) {
        // call the default updateItem function from the parent
        super.updateItem(item, empty);

        // if an item (the NoteData) was added/updated in the NoteCell, update the cell's GUI
        if (item != null && !empty) {
            // set the text of the note sample
            this.getLblSample().setText(item.getStrSample());
            
            // set the text of the date sample, with the given format in the NoteData
            this.getLblDate().setText(item.getDateFormat().format(new Date(item.getDate())));

            // set the cell's graphic with the AnchorPane created in the constructor
            this.setGraphic(this.pnlCell);
        } else {
            // if no item was added/the item was null, set no graphic in the cell
            this.setGraphic(null);
        }
    }

    /**
     * @return the main AnchorPane that'll contain the different data displays
     */
    public AnchorPane getPnlCell() {
        return pnlCell;
    }

    /**
     * @return the Label display for the note's date
     */
    public Label getLblDate() {
        return lblDate;
    }

    /**
     * @return the Label display for the note's sample text/title
     */
    public Label getLblSample() {
        return lblSample;
    }

    /**
     * Sets the label date instance
     * @param lblDate - the label instance
     */
    public void setLblDate(Label lblDate) {
        this.lblDate = lblDate;
    }

    /**
     * Sets the label sample instance
     * @param lblSample - the label instance
     */
    public void setLblSample(Label lblSample) {
        this.lblSample = lblSample;
    }

    /**
     * Sets the main pane instance
     * @param pnlCell - the anchor pane instance
     */
    public void setPnlCell(AnchorPane pnlCell) {
        this.pnlCell = pnlCell;
    }
}
