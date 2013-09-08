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
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;

import com.github.portletscanner.PortletData.Portlet;

/**
 * Generates portlet directory.
 *
 * @author Valentyn Kolesnikov
 * @version $Revision$ $Date$
 */
public class GeneratePortlet {
    private static final int PARAM_INDEX_1 = 1;
    private static final int PARAM_INDEX_2 = 2;
    private static final int PARAM_INDEX_3 = 3;
    private String[] args;

    /**
     * Constructor.
     * @param args the command line argumets
     */
    public GeneratePortlet(String[] args) {
        this.args = args;
    }

    /**
     * Generates source codes.
     * @throws Exception in case of error
     */
    public void generate() throws Exception {
        String portletFile = args[PARAM_INDEX_1].trim();
        String srcPortlet = args[PARAM_INDEX_2].trim();
        String dstPortlet = args[PARAM_INDEX_3].trim();
        LOG.info("portletFile - " + portletFile);
        PortletData portletData = null;
        try {
            JAXBContext jc = JAXBContext.newInstance(PortletData.class);
            Unmarshaller u = jc.createUnmarshaller();
            portletData = (PortletData) u.unmarshal(new FileInputStream(portletFile));
            LOG.info("Copying portlet " + srcPortlet + " to the " + dstPortlet);
            for (Portlet portlet : portletData.getPortlet()) {
                if (srcPortlet.equals(portlet.getName())) {
                    LOG.info("Portlet - " + portlet.getName());
                    new File("./" + dstPortlet).mkdirs();
                    for (Portlet.Packages.Package pack : portlet.getPackages().getPackage()) {
                        String packageDir = "./" + dstPortlet + "/" + "docroot/WEB-INF/src/com/playtech/portlet/"
                            + pack.getName().replace(".", "/");
                        new File(packageDir).mkdirs();
                        LOG.info("Package - " + pack.getName());
                        for (Portlet.Packages.Package.Class clazz : pack.getClazz()) {
                           FileUtils.writeStringToFile(new File(packageDir + "/" + clazz.getName() + ".java"),
                               new String(new DirectoryScanner().decompress(
                                   javax.xml.bind.DatatypeConverter.parseBase64Binary(clazz.getSrc()))), false);
                        }
                    }
                    String contextDir = "./" + dstPortlet + "/" + "docroot/WEB-INF/context";
                    new File(contextDir).mkdirs();
                    for (Portlet.Contextes.Context context : portlet.getContextes().getContext()) {
                       FileUtils.writeStringToFile(new File(contextDir + "/" + context.getName()),
                           new String(new DirectoryScanner().decompress(
                               javax.xml.bind.DatatypeConverter.parseBase64Binary(context.getSrc()))), false);
                    }
                }
            }
        } catch (JAXBException je) {
            LOG.error(je, je.getMessage());
        } catch (IOException ioe) {
            LOG.error(ioe, ioe.getMessage());
        }
    }
}
