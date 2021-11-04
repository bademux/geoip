package utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static java.util.Objects.nonNull;

/**
 * https://gist.github.com/bademux/88365665332630450858bb81f2a6bc9c
 */
public final class RecordingWireMock {

    private final WireMockServer wireMock;
    private final String serverUrl;
    private final boolean forceRecord;
    private final FileSource mappingsDir;
    private final FileSource filesDir;

    public RecordingWireMock(WireMockServer wireMock, String targetServerUrl, Boolean forceRecord) {
        this.wireMock = wireMock;
        this.serverUrl = targetServerUrl;
        this.forceRecord = Boolean.TRUE.equals(forceRecord);
        mappingsDir = wireMock.getOptions().filesRoot().child(MAPPINGS_ROOT);
        filesDir = wireMock.getOptions().filesRoot().child(FILES_ROOT);
    }

    public RecordingWireMock(WireMockServer wireMock, String targetServerUrl) {
        this(wireMock, targetServerUrl, forceRecordDefault());
    }

    private static boolean forceRecordDefault() {
        return nonNull(System.getProperty("--force-record"));
    }

    public RecordingWireMock start() {
        handleForceRecord();
        if (isRecordingMode()) {
            prepareFolders();
            startRecordingForDefinedServer();
        }
        return this;
    }

    public void stopRecordingIfNeeded() {
        if (RecordingStatus.Recording.equals(wireMock.getRecordingStatus().getStatus())) {
            mappingsDir.createIfNecessary();
            filesDir.createIfNecessary();
            wireMock.stopRecording();
        }
    }

    private void handleForceRecord() {
        if (forceRecord) {
            cleanup(mappingsDir);
            cleanup(filesDir);
        }
    }

    private void prepareFolders() {
        mappingsDir.createIfNecessary();
    }

    private boolean isRecordingMode() {
        return !mappingsDir.exists() || mappingsDir.listFilesRecursively().isEmpty();
    }

    private void startRecordingForDefinedServer() {
        wireMock.startRecording(recordSpec().ignoreRepeatRequests().forTarget(serverUrl).transformers("inline"));
    }

    private void cleanup(FileSource fileSource) {
        if (!fileSource.exists()) {
            return;
        }
        for (TextFile textFile : fileSource.listFilesRecursively()) {
            try {
                Files.delete(Paths.get(textFile.getPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
