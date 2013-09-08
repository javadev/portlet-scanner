/*
 * $Id$
 *
 * Copyright 2013 Valentyn Kolesnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.portletscanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.StringUtils;

import com.github.portletscanner.PortletData.Portlet.Packages.Package.Class;
import com.github.portletscanner.PortletData.Portlet.Packages.Package;

/**
 * Directory scanner.
 *
 * @author Valentyn Kolesnikov
 * @version $Revision$ $Date$
 */
public class DirectoryScanner {
    /**
     * Scans the directory and generates packages list.
     * @param dirName the directory name
     * @param scanLevel the directory scan level
     * @return the list of packages
     * @throws IOException in case of I/O error
     */
    public List<Package> scan(String dirName, int scanLevel) throws IOException {
        List<Package> result = new ArrayList<Package>();
        Collection<File> files = FileUtils.listFilesAndDirs(new File(dirName),
            TrueFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE);
        Package packRoot = new Package();
        packRoot.setName("");
        for (File file : files) {
            String addDirName = StringUtils.remove(
                file.getAbsolutePath().replace("\\", "/"), dirName).replaceFirst(".", "");
            if (StringUtils.isNotEmpty(addDirName) && StringUtils.countMatches(addDirName, "/") < scanLevel) {
                if (file.isDirectory()) {
                Collection<File> filesInDir = FileUtils.listFiles(file,
                    FileFileFilter.FILE, null);
                if (!filesInDir.isEmpty()) {
                    Package pack = new Package();
                    pack.setName(addDirName.replaceAll("\\/", "."));
                    for (File file2 : filesInDir) {
                        Class clazz = new Class();
                        clazz.setName(file2.getName().replaceFirst("\\.java$", ""));
                        clazz.setSrc(javax.xml.bind.DatatypeConverter.printBase64Binary(
                            compress(FileUtils.readFileToByteArray(file2))));
                        pack.getClazz().add(clazz);
                    }
                    result.add(pack);
                }
                } else if (StringUtils.countMatches(addDirName, "/") == 0) {
                    Class clazz = new Class();
                    clazz.setName(file.getName().replaceFirst("\\.java$", ""));
                    clazz.setSrc(javax.xml.bind.DatatypeConverter.printBase64Binary(
                        compress(FileUtils.readFileToByteArray(file))));
                    packRoot.getClazz().add(clazz);
                }
            }
        }
        if (!packRoot.getClazz().isEmpty()) {
            result.add(packRoot);
        }
        return result;
    }

    /**
     * Scans the directory and generates packages list.
     * @param dirName the directory name
     * @return the list of packages
     * @throws IOException in case of I/O error
     */
    public List<Package> scanFiles(String dirName) throws IOException {
        List<Package> result = new ArrayList<Package>();
        File file = new File(dirName);
        LOG.info("Scan folder - " + dirName);
            String addDirName = StringUtils.remove(
                file.getAbsolutePath().replace("\\", "/"), dirName).replaceFirst(".", "");
            if (file.isDirectory()) {
                Collection<File> filesInDir = FileUtils.listFiles(file,
                    FileFileFilter.FILE, null);
                if (!filesInDir.isEmpty()) {
                    Package pack = new Package();
                    pack.setName(addDirName.replaceAll("\\/", "."));
                    for (File file2 : filesInDir) {
                        Class clazz = new Class();
                        clazz.setName(file2.getName().replaceFirst("\\.java$", ""));
                        clazz.setSrc(javax.xml.bind.DatatypeConverter.printBase64Binary(
                            compress(FileUtils.readFileToByteArray(file2))));
                        pack.getClazz().add(clazz);
                    }
                    result.add(pack);
                }
            }
        return result;
    }

    /**
     * Checks is directory exists.
     * @param dirName the directory name
     * @return the true if directory exists
     */
    public boolean isExist(String dirName) {
        return new File(dirName).exists();
    }

    /**
     * Compresses the byte array.
     * @param content the byte array
     * @return the compressed byte array
     * @throws IOException in case of I/O error
     */
    public byte[] compress(byte[] content) throws IOException {
        java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
        java.util.zip.GZIPOutputStream gzipOutputStream = new java.util.zip.GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(content);
        gzipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Decompresses the byte array.
     * @param contentBytes the byte array
     * @return the decompressed byte array
     * @throws IOException in case of I/O error
     */
    public byte[] decompress(byte[] contentBytes) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        IOUtils.copy(new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(contentBytes)), out);
        return out.toByteArray();
    }
}
