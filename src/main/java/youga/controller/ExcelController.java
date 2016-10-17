package youga.controller;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import youga.Main;
import youga.model.Excel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Created by Youga on 2016/2/15.
 */
public class ExcelController extends AnchorPane implements Initializable {

    private Main app;
    @FXML
    TableView table;
    @FXML
    Button importBtn;
    @FXML
    Label label;
    private String mOutPath;

    public void setApp(Main app) {
        this.app = app;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void onImport(ActionEvent actionEvent) {
        if (mOutPath == null) {
            label.setText("请先选择输出路径!");
            return;
        }

        //设置界面风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        JFileChooser fileChooser = new JFileChooser();
        //设置选择路径模式
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        //设置对话框标题
        fileChooser.setDialogTitle("请选择Excel文件");
        //设置过滤器
        fileChooser.setFileFilter(new ExcelFilter());

        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {//用户点击了确定
            String path = fileChooser.getSelectedFile().getAbsolutePath();//取得路径选择
            System.out.println("path:" + path);
            try {
                readCalendarExcel(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ObservableList<Map<String, String>> mData = FXCollections.observableArrayList();

    static List<String> mNumbers = new ArrayList<>();
    static List<String> mExtra = new ArrayList<>();
    static List<String> mMonths = new ArrayList<>();

    static {
        mExtra = new ArrayList<>();
        mExtra.add("初");
        mExtra.add("十");
        mExtra.add("廿");
        mExtra.add("三");

        mNumbers.add("零");
        mNumbers.add("一");
        mNumbers.add("二");
        mNumbers.add("三");
        mNumbers.add("四");
        mNumbers.add("五");
        mNumbers.add("六");
        mNumbers.add("七");
        mNumbers.add("八");
        mNumbers.add("九");
        mNumbers.add("十");

        mMonths.add("零");
        mMonths.add("正");
        mMonths.add("二");
        mMonths.add("三");
        mMonths.add("四");
        mMonths.add("五");
        mMonths.add("六");
        mMonths.add("七");
        mMonths.add("八");
        mMonths.add("九");
        mMonths.add("十");
        mMonths.add("十一");
        mMonths.add("十二");
    }


    /**
     * 读取Excel测试，兼容 Excel 2003/2007/2010
     */
    private void readCalendarExcel(String path) throws IOException {
        FileInputStream fis = new FileInputStream(new File(path));
        XSSFWorkbook book = new XSSFWorkbook(fis);
        XSSFSheet sheet = book.getSheetAt(0);
        Iterator<Row> itr = sheet.iterator();
        List<Calendar> calendarSet = new ArrayList<>();
        List<String> stringSet = new ArrayList<>();
        while (itr.hasNext()) {
            Row row = itr.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            List<String> rowArray = new ArrayList<>();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_STRING:
                        String cellString = cell.getStringCellValue();
                        if (!cellString.isEmpty()) {
                            cellString = cellString.replaceAll("\\s*", "");
                            rowArray.add(cellString);
                        }
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        int cellInt = (int) cell.getNumericCellValue();
                        rowArray.add(String.valueOf(cellInt));
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        rowArray.add(String.valueOf(cell.getBooleanCellValue()));
                        break;
                    default:
                }
            }
            if (!rowArray.isEmpty() && stringSet.isEmpty() && "公曆日期".equals(rowArray.get(0))) {
                stringSet.addAll(rowArray);
            }
            if (!rowArray.isEmpty() && rowArray.get(0).endsWith("月")) {
                String solarMonth = rowArray.get(0).substring(0, rowArray.get(0).length() - 1);
                boolean first = true;
                for (int i = 2; i < rowArray.size() - 1; i++) {
                    String lunarDay = rowArray.get(i);
                    int lunarMonth = 0;
                    int leapMonth = 0;
                    if (lunarDay.endsWith("月")) {
                        if (lunarDay.startsWith("閏")) {
                            lunarDay = lunarDay.replace("閏", "");
                            lunarMonth = 13;
                            leapMonth = mMonths.indexOf(lunarDay.substring(0, lunarDay.indexOf("月")));
                        } else {
                            lunarMonth = mMonths.indexOf(lunarDay.substring(0, lunarDay.indexOf("月")));
                        }
                        lunarDay = "初一";
                    } else {
                        if (!calendarSet.isEmpty())
                            lunarMonth = calendarSet.get(calendarSet.size() - 1).lunarMonth;
                    }
                    String s = lunarDay.substring(0, 1);
                    String e = lunarDay.substring(1, 2);
                    if (calendarSet.isEmpty()) {
                        calendarSet.add(new Calendar(1901, Integer.valueOf(solarMonth), Integer.valueOf(stringSet.get(i - 1)),
                                1900, 11, mExtra.indexOf(s) * 10 + mNumbers.indexOf(e), leapMonth));
                    } else {
                        Calendar calendar = calendarSet.get(calendarSet.size() - 1);
                        int lunarYear, solarYear;
                        if ("正月".equals(rowArray.get(i))) {
                            lunarYear = calendar.lunarYear + 1;
                        } else {
                            lunarYear = calendar.lunarYear;
                        }
                        if ("1月".equals(rowArray.get(0)) && calendar.solarMonth == 12 && first) {
                            solarYear = calendar.solarYear + 1;
                            first = false;
                        } else {
                            solarYear = calendar.solarYear;
                        }
                        calendarSet.add(new Calendar(solarYear, Integer.valueOf(solarMonth), Integer.valueOf(stringSet.get(i - 1)),
                                lunarYear, lunarMonth, (mExtra.indexOf(s) == -1) ? 20 :
                                mExtra.indexOf(s) * 10 + (mExtra.indexOf(s) == 3 ? 0 : mNumbers.indexOf(e)), leapMonth));
                    }
                }
            }
        }

        Map<Integer, List<Calendar>> map = new LinkedHashMap<>();
        for (Calendar c : calendarSet) {
            if (!map.containsKey(c.lunarYear)) {
                List<Calendar> list = new ArrayList<>();
                list.add(c);
                map.put(c.lunarYear, list);
            } else {
                List<Calendar> list = map.get(c.lunarYear);
                list.add(c);
            }
        }

        Map<Integer, String> yearMap = new LinkedHashMap<>();
        for (Integer year : map.keySet()) {
            Map<Integer, Integer> month = new HashMap<>();
            Calendar calendar = null;
            for (Calendar c : map.get(year)) {
                if (!month.containsKey(c.lunarMonth)) {
                    month.put(c.lunarMonth, 1);
                } else {
                    int days = month.get(c.lunarMonth);
                    ++days;
                    month.put(c.lunarMonth, days);
                }
                if (c.leapMonth != 0) {
                    calendar = c;
                }
            }
            StringBuilder buffer = new StringBuilder();
            if (calendar != null) buffer.append(month.get(13) == 30 ? "1" : "0");
            for (Integer m : month.keySet()) {
                if (m != 13) buffer.append(month.get(m) == 30 ? "1" : "0");
            }
            String binaryLeap = Integer.toBinaryString(calendar == null ? 0 : calendar.leapMonth);
            if (binaryLeap.length() == 4) {
                buffer.append(binaryLeap);
            } else {
                for (int i = 0; i < (4 - binaryLeap.length()); i++) {
                    buffer.append("0");
                }
                buffer.append(binaryLeap);
            }

            yearMap.put(year, buffer.toString());
        }

        List<String> hexList = new ArrayList<>();
        for (Integer year : yearMap.keySet()) {
            hexList.add("0x" + Integer.toHexString(Integer.valueOf(yearMap.get(year), 2)));
        }
        System.out.println("hexList:" + hexList.toString());
        writeJson(new Gson().toJson(calendarSet), "1901-2100年公农历.json");
        writeJson(new Gson().toJson(hexList), "1901-2100年公农历(16进制数组).text");
        label.setText("输出完毕,请取文件夹中查看");
    }

    void writeJson(String json, String fileName) {

        try {
            FileOutputStream out = new FileOutputStream(new File(mOutPath, fileName));
            out.write(json.getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onOutPath(ActionEvent actionEvent) {
        //设置界面风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        JFileChooser fileChooser = new JFileChooser();
        //设置选择路径模式
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //设置对话框标题
        fileChooser.setDialogTitle("请选择路径");

        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {//用户点击了确定
            mOutPath = fileChooser.getSelectedFile().getAbsolutePath();//取得路径选择
            label.setText("输出路径:" + mOutPath);
        }
    }


    public static class Calendar {
        public int solarYear, solarMonth, solarDay;
        public int lunarYear, lunarMonth, lunarDay;
        public int leapMonth;

        public Calendar(int solarYear, int solarMonth, int solarDay, int lunarYear, int lunarMonth, int lunarDay, int leapMonth) {
            this.solarYear = solarYear;
            this.solarMonth = solarMonth;
            this.solarDay = solarDay;
            this.lunarYear = lunarYear;
            this.lunarMonth = lunarMonth;
            this.lunarDay = lunarDay;
            this.leapMonth = leapMonth;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Calendar)) return false;

            Calendar calendar = (Calendar) o;

            if (solarYear != calendar.solarYear) return false;
            if (solarMonth != calendar.solarMonth) return false;
            if (solarDay != calendar.solarDay) return false;
            if (lunarYear != calendar.lunarYear) return false;
            if (lunarMonth != calendar.lunarMonth) return false;
            if (lunarDay != calendar.lunarDay) return false;
            return leapMonth == calendar.leapMonth;

        }

        @Override
        public int hashCode() {
            int result = solarYear;
            result = 31 * result + solarMonth;
            result = 31 * result + solarDay;
            result = 31 * result + lunarYear;
            result = 31 * result + lunarMonth;
            result = 31 * result + lunarDay;
            result = 31 * result + leapMonth;
            return result;
        }

        @Override
        public String toString() {
            return "Calendar{" +
                    "solarYear=" + solarYear +
                    ", solarMonth=" + solarMonth +
                    ", solarDay=" + solarDay +
                    ", lunarYear=" + lunarYear +
                    ", lunarMonth=" + lunarMonth +
                    ", lunarDay=" + lunarDay +
                    ", leapMonth=" + leapMonth +
                    '}';
        }
    }


//    /**
//     * 读取Excel测试，兼容 Excel 2003/2007/2010
//     */
//    private void readExcel(String path) {
//        File excel = new File(path);
//        mData.clear();
//        try {
//            FileInputStream fis = new FileInputStream(excel);
//            XSSFWorkbook book = new XSSFWorkbook(fis);
//            XSSFSheet sheet = book.getSheetAt(0);
//            Iterator<Row> itr = sheet.iterator();
//            int rowNum = -1;
//            List<String> firstArray = new ArrayList<>();
//            while (itr.hasNext()) {
//                Row row = itr.next();
//                rowNum++;
//                Iterator<Cell> cellIterator = row.cellIterator();
//                List<String> rowArray = new ArrayList<>();
//                while (cellIterator.hasNext()) {
//                    Cell cell = cellIterator.next();
//                    switch (cell.getCellType()) {
//                        case Cell.CELL_TYPE_STRING:
//                            rowArray.add(cell.getStringCellValue());
//                            break;
//                        case Cell.CELL_TYPE_NUMERIC:
//                            rowArray.add(String.valueOf(cell.getNumericCellValue()));
//                            break;
//                        case Cell.CELL_TYPE_BOOLEAN:
//                            rowArray.add(String.valueOf(cell.getBooleanCellValue()));
//                            break;
//                        default:
//                    }
//                }
//                if (rowNum == 0) {
//                    firstArray = rowArray;
//                } else {
//                    Map<String, String> map = new HashMap<>();
//                    for (String text : firstArray) {
//                        if (rowArray.size() > firstArray.indexOf(text) && !rowArray.get(firstArray.indexOf(text)).isEmpty())
//                            map.put(text, rowArray.get(firstArray.indexOf(text)));
//                    }
//                    if (map.size() > 0) mData.add(map);
//                }
//            }
//
//            table.setItems(mData);
//            ArrayList<TableColumn<Map, String>> tableColumns = new ArrayList<>();
//            for (String title : firstArray) {
//                TableColumn<Map, String> tableColumn = new TableColumn<>(title);
//                tableColumn.setCellValueFactory(new MapValueFactory<>(title));
//                tableColumn.setMinWidth(130);
//                tableColumns.add(tableColumn);
//            }
//
//            table.getColumns().setAll(tableColumns);
//            Callback<TableColumn<Map, String>, TableCell<Map, String>> cellFactoryForMap =
//                    p -> new TextFieldTableCell(new StringConverter() {
//                        @Override
//                        public String toString(Object t) {
//                            return (t == null) ? "" : t.toString();
//                        }
//
//                        @Override
//                        public Object fromString(String string) {
//                            return string;
//                        }
//                    });
//
//            for (TableColumn<Map, String> tableColumn : tableColumns) {
//                tableColumn.setCellFactory(cellFactoryForMap);
//            }
//
//            fis.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }


    private void writeExcel(String path) {
        File excel = new File(path);
        try {
            FileInputStream fis = new FileInputStream(excel);
            XSSFWorkbook book = new XSSFWorkbook(fis);
            XSSFSheet sheet = book.getSheetAt(0);

            // writing data into XLSX file
            Map<String, Object[]> newData = new HashMap<>();
            newData.put("7", new Object[]{7d, "Sonya", "75K", "SALES", "Rupert"});
            newData.put("8", new Object[]{8d, "Kris", "85K", "SALES", "Rupert"});
            newData.put("9", new Object[]{9d, "Dave", "90K", "SALES", "Rupert"});
            Set<String> newRows = newData.keySet();
            int rownum = sheet.getLastRowNum();

            for (String key : newRows) {
                Row row = sheet.createRow(rownum++);
                Object[] objArr = newData.get(key);
                int cellnum = 0;
                for (Object obj : objArr) {
                    Cell cell = row.createCell(cellnum++);
                    if (obj instanceof String) {
                        cell.setCellValue((String) obj);
                    } else if (obj instanceof Boolean) {
                        cell.setCellValue((Boolean) obj);
                    } else if (obj instanceof Date) {
                        cell.setCellValue((Date) obj);
                    } else if (obj instanceof Double) {
                        cell.setCellValue((Double) obj);
                    }
                }
            }
            // open an OutputStream to save written data into Excel file

            FileOutputStream os = new FileOutputStream(excel);
            book.write(os);
            System.out.println("Writing on Excel file Finished ...");

            // Close workbook, OutputStream and Excel file to prevent leak
            os.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ExcelFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".xlsx") || f.getName().endsWith(".xls");
        }

        @Override
        public String getDescription() {
            return "Excel表格(*.xls, *.xlsx)";
        }
    }
}
