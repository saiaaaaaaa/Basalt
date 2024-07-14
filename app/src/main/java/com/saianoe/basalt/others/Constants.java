package com.saianoe.basalt.others;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** @noinspection ALL*/
public class Constants {
    public static final String homeDB = "homeDB";
    public static final String readDB = "readDB";
    public static final String downloadedDB = "downloadedDB";
    public static final String updateDB = "updateDB";
    public static final String homeTable = "homeTable";
    public static final String readTable = "readTable";
    public static final String downloadedTable = "downloadedTable";
    public static final String updateTable = "updateTable";
    public static final String[] homeTableColumns = {"h_m_id", "h_m_title", "h_m_cover"};
    public static final String[] readTableColumns = {"r_m_id", "r_ch_id_and_title"};
    public static final String downloadedTableColumn = "d_m_id_and_ch_id_and_title";
    public static final String[] updateTableColumns = {"u_m_id", "u_ch_id_and_title"};
    public static final String[] intentFromManga = {"mangaId", "mangaTitle", "mangaCover"};
    public static final String tableColumnType = "text";
    public static final String[] intentFromChapter = {"manga_chapter_position", "manga_chapters", "manga_chapters_titles"};
    public static final String baseUrl = "https://api.mangadex.org/";
    public static final String homeUrl = baseUrl + "manga/";
    public static final String searchUrl = baseUrl + "manga?title=";
    public static final String coverUrl = baseUrl + "cover/";
    public static final String feedUrl = baseUrl + "at-home/server/";
    public static final String coverPictureUrl = "https://uploads.mangadex.org/covers/";
    public static final int downloadPermissionCode = 700;
    public static final int downloadPathCode = 701;
    public static final int notificationChannelCode = 702;
    public static final int dataExportCode = 703;
    public static final int dataImportCode = 704;
    public static final int updateNotificationChannelCode = 704;
    public static final Comparator defaultComparator = (o1, o2) -> {
        try {
            double a = Double.parseDouble((String) o1);
            double b = Double.parseDouble((String) o2);
            return Double.compare(a, b);
        } catch (Exception ignored){
            return 0;
        }
    };
    public static SharedPreferences prefs;
    public static final List<String> notificationDownloadStatusList = new ArrayList<>();
    public static final List<String> notificationUpdateStatusList = new ArrayList<>();
    public static SharedPreferences.Editor prefsEditor;
    public static final String prefsSettings = "basalt.settings";
    public static final String intentTypeData = "*/*";
    public static final String atSeparator = "@";
    public static final String forwardSlash = "/";
    public static final String nullString = "null";
    public static final String negOne = "-1";
    public static final String zero = "0";
    public static final String one = "1";
    public static final String outDataHome = "HOME";
    public static final String outDataRead = "READ";
    public static final String chapterEND = "CHAPTER_END";
    public static final String chapterSTART = "CHAPTER_START";
    public static final String dataSaver = "data_saver";
    public static final String typeDataSaver = "dataSaver";
    public static final String typeDataSaver2 = "data-saver";
    public static final String typeData = "data";
    public static final String typeAttributes = "attributes";
    public static final String typeRelationships = "relationships";
    public static final String typeType = "type";
    public static final String typeCoverArt = "cover_art";
    public static final String typeId = "id";
    public static final String typeFilename = "fileName";
    public static final String picRes = ".256.jpg";
    public static final String enableDownloads = "enable_downloads";
    public static final String enableDownloadsDataSaver = "enable_downloads_data_saver";
    public static final String downloadPathString = "download_path";
    public static final String downloadPathNotSet = "No path set.";
    public static final String webGet = "GET";
    public static final String functionRequiresStorageAccess = "This function requires access to device storage.";
    public static List<String> home_manga_ids = new ArrayList<>(), home_manga_titles = new ArrayList<>(), home_manga_covers = new ArrayList<>();
}
