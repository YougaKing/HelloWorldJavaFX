package youga.model;

import javafx.beans.property.SimpleStringProperty;

import java.util.List;

/**
 * Created by Youga on 2016/2/17.
 */
public class Excel {

    SimpleStringProperty row0;
    SimpleStringProperty row1;
    SimpleStringProperty row2;
    SimpleStringProperty row3;
    SimpleStringProperty row4;

    public Excel(List<String> columnArray) {
        row0 = new SimpleStringProperty(columnArray.get(0));
        row0 = new SimpleStringProperty(columnArray.get(1));
        row0 = new SimpleStringProperty(columnArray.get(2));
        row0 = new SimpleStringProperty(columnArray.get(3));
        row0 = new SimpleStringProperty(columnArray.get(4));
    }

    public String getRow0() {
        return row0.get();
    }

    public SimpleStringProperty row0Property() {
        return row0;
    }

    public String getRow1() {
        return row1.get();
    }

    public SimpleStringProperty row1Property() {
        return row1;
    }

    public String getRow2() {
        return row2.get();
    }

    public SimpleStringProperty row2Property() {
        return row2;
    }

    public String getRow3() {
        return row3.get();
    }

    public SimpleStringProperty row3Property() {
        return row3;
    }

    public String getRow4() {
        return row4.get();
    }

    public SimpleStringProperty row4Property() {
        return row4;
    }
}
