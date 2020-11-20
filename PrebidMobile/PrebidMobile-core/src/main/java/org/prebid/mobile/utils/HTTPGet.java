/*
 *    Copyright 2020-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.utils;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public abstract class HTTPGet {


    private HTTPGetAsync httpGetAsync;
    private boolean isCancelled = false;

    public HTTPGet() {
        super();
    }

    abstract protected void onPostExecute(HTTPResponse response);

    public void execute() {
        httpGetAsync = new HTTPGetAsync();
        httpGetAsync.execute();
    }

    protected abstract String getUrl();

    private HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(false);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("GET");
        return connection;
    }

    private void setConnectionParams(HttpURLConnection connection) throws ProtocolException {
        connection.setConnectTimeout(Util.HTTP_CONNECTION_TIMEOUT);
        connection.setReadTimeout(Util.HTTP_SOCKET_TIMEOUT);
    }

    protected HTTPResponse makeHttpRequest() {
        HTTPResponse out = new HTTPResponse();
        HttpURLConnection connection = null;
        try {
            URL reqUrl = new URL(getUrl());
            if (reqUrl.getHost() == null) {
                out.setSucceeded(false);
                return out;
            }
            //  Create and connect to HTTP service
            connection = createConnection(reqUrl);
            setConnectionParams(connection);
            connection.connect();


            //Response parsing
            StringBuilder builder = new StringBuilder();
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            is.close();
            String responseString = builder.toString();


            out.setHeaders(connection.getHeaderFields());
            out.setResponseBody(responseString);
            boolean isStatusOK = (connection.getResponseCode()
                    == HttpURLConnection.HTTP_OK);
            out.setSucceeded(isStatusOK);

        } catch (MalformedURLException e) {
            out.setSucceeded(false);
            out.setErrorCode(HttpErrorCode.URI_SYNTAX_ERROR);
        } catch (IOException e) {
            out.setSucceeded(false);
            out.setErrorCode(HttpErrorCode.TRANSPORT_ERROR);
        } catch (Exception e) {
            out.setSucceeded(false);
            out.setErrorCode(HttpErrorCode.UNKNOWN_ERROR);
            e.printStackTrace();
        }
        return out;
    }

    public void cancel(boolean b) {
        if (httpGetAsync != null) {
            httpGetAsync.cancel(true);
            isCancelled = true;
        }
    }

    protected HTTPResponse doInBackground(Void[] params) {
        return makeHttpRequest();
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    private class HTTPGetAsync extends AsyncTask<Void, Void, HTTPResponse> {


        public HTTPGetAsync() {
            super();
        }

        @Override
        protected HTTPResponse doInBackground(Void... params) {
            return makeHttpRequest();
        }

        @Override
        protected void onPostExecute(HTTPResponse response) {
            HTTPGet.this.onPostExecute(response);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onCancelled(HTTPResponse response) {
            super.onCancelled(null);
        }


        protected String getUrl() {
            return HTTPGet.this.getUrl();
        }

    }
}

