/*
   Copyright 2018 Manpreet Singh

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.src.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ConfigManager {

    private Map<String, ConfigParser> mConfigMap;
    private Properties mConfigReader;

    public ConfigManager(InputStream inputStream) throws Exception{
        this.mConfigMap = new TreeMap<>();
        mConfigReader = new Properties();

        mConfigReader.load(inputStream);

        for (String s : mConfigReader.getProperty("config_filesnames").split(",")) {
            if (!s.isEmpty()) {
                mConfigMap.put(s.split(".")[0], new ConfigParser(s));
            }
        }
    }

    private Map dataMap() {
        return this.mConfigMap;
    }
}