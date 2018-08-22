/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.util;

import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Torridity
 */
public final class GithubVersionCheck {
    private static final Logger LOGGER = LogManager.getLogger("GithubVersionCheck");

    private static final String API_URL = "https://api.github.com/repos/torridity/dsworkbench/releases/latest";

    public static class UpdateInfo {

        private static UPDATE_STATUS status;
        private static String downloadUrl;

        public static UpdateInfo factoryUpdateAvailableInfo(String pDownloadUrl) {
            return new UpdateInfo(UPDATE_STATUS.UPDATE_AVAILABLE, pDownloadUrl);
        }

        public static UpdateInfo factoryNoUpdateAvailableInfo() {
            return new UpdateInfo(UPDATE_STATUS.NO_UPDATE_AVAILABLE, null);
        }

        public static UpdateInfo factoryUpdateCheckFailedInfo() {
            return new UpdateInfo(UPDATE_STATUS.CHECK_FAILED, null);
        }

        UpdateInfo(UPDATE_STATUS pStatus, String pDownloadUrl) {
            status = pStatus;
            downloadUrl = pDownloadUrl;
        }

        public UPDATE_STATUS getStatus() {
            return status;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

    }

    public enum UPDATE_STATUS {

        UPDATE_AVAILABLE,
        NO_UPDATE_AVAILABLE,
        CHECK_FAILED

    }

    /**
     * Check on github whether there is an update for DS Workbench available.
     * Therefor, the last release tag is obtained and compared to the DS
     * Workbench version stored in {@link Constants}. The actual version
     * consists of {@link Constants#VERSION} and
     * {@link Constants#VERSION_ADDITION} where VERSION is a double value and
     * VERSION_ADDITION a string, e.g. 1.0beta. The returned UpdateInfo reflects
     * the result of the request. If the update check succeeds and an update is
     * available, the result also contains the download URL. If the update check
     * fails, e.g. due to missing internet connectivity or due to a GitHub API
     * change, the according enum will be used.
     *
     * @return The update information object including the status and download
     * URL, if available.
     */
    public static UpdateInfo getUpdateInformation() {
        try {
            URLConnection u = new URL(API_URL).openConnection(DSWorkbenchSettingsDialog.getSingleton().getWebProxy());
            InputStream in = u.getInputStream();
            int bytes = 0;
            byte[] data = new byte[1024];
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            while ((bytes = in.read(data)) != -1) {
                result.write(data, 0, bytes);
            }
            
            JSONObject latestRelease = new JSONObject(new String(result.toByteArray()));
            String latestTagName = (String) latestRelease.get("tag_name");

            String ownVersion = Double.toString(Constants.VERSION) + Constants.VERSION_ADDITION;
            if (ownVersion.equals(latestTagName)) {
                //no update available
                return UpdateInfo.factoryNoUpdateAvailableInfo();
            }
            //update available...obtain download URL and return
            String downloadUrl = (String) ((JSONObject) ((JSONArray) latestRelease.get("assets")).get(0)).get("browser_download_url");
            return UpdateInfo.factoryUpdateAvailableInfo(downloadUrl);
        } catch (IOException | JSONException ex) {
            //failed to check update
            LOGGER.warn("Failed to check for update.", ex);
            return UpdateInfo.factoryUpdateCheckFailedInfo();
        }
    }
}
