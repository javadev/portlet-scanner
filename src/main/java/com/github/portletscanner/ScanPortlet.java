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

import java.util.ArrayList;
import java.util.List;

import com.github.portletscanner.PortletData.Portlet.Packages.Package;
import com.github.portletscanner.PortletData.Portlet;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * Scans portlet directory.
 *
 * @author Valentyn Kolesnikov
 * @version $Revision$ $Date$
 */
public class ScanPortlet {
    private static final int PARAM_INDEX_0 = 0;
    private static final int PARAM_INDEX_1 = 1;
    private static final int PARAM_INDEX_2 = 2;
    private static final int PARAM_COUNT_3 = 3;
    private static final int DIRECTORY_DEEP_LEVEL_1 = 1;
    private static final int DIRECTORY_DEEP_LEVEL_3 = 3;
    private String[] args;
    /**
     * Constructor.
     * @param args the command line arguments
     */
    public ScanPortlet(String[] args) {
        this.args = args;
    }

    /**
     * Displays help and executes scanner or generator.
     * @param args the command line arguments
     * @throws Exception in case of error
     */
    public static void main(String[] args) throws Exception {
        if (args.length < PARAM_COUNT_3) {
            LOG.info("Scan portlet.");
            LOG.info("Usage: java -jar portletscanner-1.0 scan portlet_directory output.xml");
            LOG.info("or: java -jar portletscanner-1.0 generate config.xml portlet_name new_portlet_name");
            return;
        }
        if (args[PARAM_INDEX_0].trim().equalsIgnoreCase("scan")) {
            new ScanPortlet(args).scan();
        } else if (args[PARAM_INDEX_0].trim().equalsIgnoreCase("generate")) {
            new GeneratePortlet(args).generate();
        }
    }

    /**
     * Scans the directories.
     * @throws Exception in case of error
     */
    public void scan() throws Exception {
        String portletDirectory = args[PARAM_INDEX_1].trim();
        String outXml = args[PARAM_INDEX_2].trim();
        LOG.info("portlet directory:" + portletDirectory);
        LOG.info("out xml:" + outXml);
        List<Package> directories = new DirectoryScanner().scan(portletDirectory, DIRECTORY_DEEP_LEVEL_1);
        List<Portlet> portlets = new ArrayList<Portlet>();
        for (Package dir : directories) {
            LOG.info(" --> " + dir.getName());
            if ("".equals(dir.getName())) {
                continue;
            }
            Portlet portlet = new Portlet();
            portlet.setName(dir.getName());
            portlets.add(portlet);
            if (new DirectoryScanner().isExist(
                portletDirectory + "/" + dir.getName() + "/docroot/WEB-INF/src/com/playtech/portlet")) {
                List<Package> packages = new DirectoryScanner().scan(
                    portletDirectory + "/" + dir.getName() + "/docroot/WEB-INF/src/com/playtech/portlet",
                        DIRECTORY_DEEP_LEVEL_3);
                LOG.info(packages.toString());
                Portlet.Packages packages1 = new Portlet.Packages();
                packages1.getPackage().addAll(packages);
                portlet.setPackages(packages1);
            }
            if (new DirectoryScanner().isExist(portletDirectory + "/" + dir.name + "/docroot/jsp")) {
                List<Package> packages = new DirectoryScanner().scan(
                    portletDirectory + "/" + dir.name + "/docroot/jsp", DIRECTORY_DEEP_LEVEL_3);
                LOG.info(packages.toString());
                for (Package pack : packages) {
                    Portlet.Jsps.Jsp jsp = new Portlet.Jsps.Jsp();
                    for (Portlet.Packages.Package.Class clazz : pack.getClazz()) {
                        Portlet.Jsps.Jsp.File file = new Portlet.Jsps.Jsp.File();
                        file.setName(clazz.getName());
                        file.setSrc(clazz.getSrc());
                        jsp.getFile().add(file);
                    }
                    Portlet.Jsps jsps = new Portlet.Jsps();
                    jsps.getJsp().add(jsp);
                    portlet.setJsps(jsps);
                }
            }
            if (new DirectoryScanner().isExist(portletDirectory + "/" + dir.name + "/docroot/js")) {
                List<Package> packages = new DirectoryScanner().scan(
                    portletDirectory + "/" + dir.name + "/docroot/js", DIRECTORY_DEEP_LEVEL_3);
                LOG.info(packages.toString());
                for (Package pack : packages) {
                    Portlet.Jses.Js js = new Portlet.Jses.Js();
                    for (Portlet.Packages.Package.Class clazz : pack.getClazz()) {
                        Portlet.Jses.Js.File file = new Portlet.Jses.Js.File();
                        file.setName(clazz.getName());
                        file.setSrc(clazz.getSrc());
                        js.getFile().add(file);
                    }
                    Portlet.Jses jses = new Portlet.Jses();
                    jses.getJs().add(js);
                    portlet.setJses(jses);
                }
            }
            if (new DirectoryScanner().isExist(portletDirectory + "/" + dir.name + "/docroot/css")) {
                List<Package> packages = new DirectoryScanner().scan(
                    portletDirectory + "/" + dir.name + "/docroot/css", DIRECTORY_DEEP_LEVEL_3);
                LOG.info(packages.toString());
                for (Package pack : packages) {
                    Portlet.Csses.Css css = new Portlet.Csses.Css();
                    for (Portlet.Packages.Package.Class clazz : pack.getClazz()) {
                        Portlet.Csses.Css.File file = new Portlet.Csses.Css.File();
                        file.setName(clazz.getName());
                        file.setSrc(clazz.getSrc());
                        css.getFile().add(file);
                    }
                    Portlet.Csses csses = new Portlet.Csses();
                    csses.getCss().add(css);
                    portlet.setCsses(csses);
                }
            }
            if (new DirectoryScanner().isExist(portletDirectory + "/" + dir.name + "/docroot/WEB-INF/context")) {
                List<Package> packages = new DirectoryScanner().scanFiles(
                    portletDirectory + "/" + dir.name + "/docroot/WEB-INF/context");
                for (Package pack : packages) {
                    for (Portlet.Packages.Package.Class clazz : pack.getClazz()) {
                        Portlet.Contextes.Context context = new Portlet.Contextes.Context();
                        context.setName(clazz.getName());
                        context.setSrc(clazz.getSrc());
                        if (portlet.getContextes() == null) {
                            portlet.setContextes(new Portlet.Contextes());
                        }
                        portlet.getContextes().getContext().add(context);
                    }
                }
            }
            if (new DirectoryScanner().isExist(portletDirectory + "/" + dir.name + "/docroot/WEB-INF/tags")) {
                List<Package> packages = new DirectoryScanner().scanFiles(
                    portletDirectory + "/" + dir.name + "/docroot/WEB-INF/tags");
                LOG.info("tags - " + packages.toString());
                for (Package pack : packages) {
                    for (Portlet.Packages.Package.Class clazz : pack.getClazz()) {
                        Portlet.Tags.Tag tag = new Portlet.Tags.Tag();
                        tag.setName(clazz.getName());
                        tag.setSrc(clazz.getSrc());
                        if (portlet.getTags() == null) {
                            portlet.setTags(new Portlet.Tags());
                        }
                        portlet.getTags().getTag().add(tag);
                    }
                }
            }
            if (new DirectoryScanner().isExist(portletDirectory + "/" + dir.name + "/docroot/WEB-INF/tld")) {
                List<Package> packages = new DirectoryScanner().scanFiles(
                    portletDirectory + "/" + dir.name + "/docroot/WEB-INF/tld");
                LOG.info("tlds - " + packages.toString());
                for (Package pack : packages) {
                    for (Portlet.Packages.Package.Class clazz : pack.getClazz()) {
                        Portlet.Tlds.Tld tld = new Portlet.Tlds.Tld();
                        tld.setName(clazz.getName());
                        tld.setSrc(clazz.getSrc());
                        if (portlet.getTlds() == null) {
                            portlet.setTlds(new Portlet.Tlds());
                        }
                        portlet.getTlds().getTld().add(tld);
                    }
                }
            }
        }
        Writer writer = new OutputStreamWriter(new FileOutputStream(outXml), "utf-8");
        JAXBContext context = JAXBContext.newInstance(PortletData.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        PortletData portletData = new PortletData();
        portletData.getPortlet().addAll(portlets);
        m.marshal(portletData, writer);
    }
}
