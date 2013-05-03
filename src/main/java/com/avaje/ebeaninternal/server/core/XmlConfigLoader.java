package com.avaje.ebeaninternal.server.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.api.ClassUtil;
import com.avaje.ebeaninternal.server.lib.util.Dnode;
import com.avaje.ebeaninternal.server.lib.util.DnodeReader;
import com.avaje.ebeaninternal.server.util.ClassPathReader;
import com.avaje.ebeaninternal.server.util.DefaultClassPathReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to read the orm.xml and ebean-orm.xml configuration files.
 * 
 * @author rbygrave
 */
public class XmlConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(XmlConfigLoader.class);
    
    private final ClassPathReader classPathReader;

    private final Object[] classPaths;
  
    
    public XmlConfigLoader(ClassLoader classLoader){
        
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        
        String cn = GlobalProperties.get("ebean.classpathreader", null);
        if (cn != null){
            // use a user defined classPathReader
            logger.info("Using ["+cn+"] to read the searchable class path");
            this.classPathReader = (ClassPathReader)ClassUtil.newInstance(cn, this.getClass());
        } else {
            this.classPathReader = new DefaultClassPathReader();
        }
        
        this.classPaths = classPathReader.readPath(classLoader);
    }
    
    public XmlConfig load() {
        List<Dnode> ormXml = search("META-INF/orm.xml");
        List<Dnode> ebeanOrmXml = search("META-INF/ebean-orm.xml");
        
        return new XmlConfig(ormXml, ebeanOrmXml);
    }
    
    public List<Dnode> search(String searchFor) {
        
        ArrayList<Dnode> xmlList = new ArrayList<Dnode>();

        String charsetName = Charset.defaultCharset().name();

        for (int h = 0; h < classPaths.length; h++) {

            try {
                // for each class path ...
                File classPath;
                if (URL.class.isInstance(classPaths[h])) {
                    classPath = new File(((URL) classPaths[h]).getFile());
                } else {
                    classPath = new File(classPaths[h].toString());
                }

                // URL Decode the path replacing %20 to space characters.
                String path = URLDecoder.decode(classPath.getAbsolutePath(), charsetName);

                classPath = new File(path);

                if (classPath.isDirectory()) {
                    checkDir(searchFor, xmlList, classPath);

                } else if (classPath.getName().endsWith(".jar") || classPath.getName().endsWith(".war") || classPath.getName().endsWith(".war!/WEB-INF/classes")) {
                    checkJar(searchFor, xmlList, classPath);
                  
                } else {
                    // this is not expected
                    String msg = "Not a Jar or Directory? " + classPath.getAbsolutePath();
                    logger.error(msg);
                }

            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return xmlList;
        
    }

    private void processInputStream(ArrayList<Dnode> xmlList, InputStream is) throws IOException {
      
        DnodeReader reader = new DnodeReader();
        Dnode xmlDoc = reader.parseXml(is);
        is.close();
        
        xmlList.add(xmlDoc);
    }
    
    private void checkFile(String searchFor, ArrayList<Dnode> xmlList, File dir) throws IOException {

        File f = new File(dir, searchFor);
        if (f.exists()){
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream is = new BufferedInputStream(fis);
            processInputStream(xmlList, is);
        }
    }
    
    private void checkDir(String searchFor, ArrayList<Dnode> xmlList, File dir) throws IOException {

        checkFile(searchFor, xmlList, dir);
        
        if (dir.getPath().endsWith("classes")) {
            // see if this is part of webapp and look for META-INF/searchFor
            // relative to the WEB-INF/classes directory
            File parent = dir.getParentFile();
            if (parent != null && parent.getPath().endsWith("WEB-INF")){
                parent = parent.getParentFile();
                if (parent != null){
                    File metaInf = new File(parent, "META-INF");
                    if (metaInf.exists()){
                        checkFile(searchFor, xmlList, metaInf);
                    }
                }
            }
        }
    }
    
    private void checkJar(String searchFor, ArrayList<Dnode> xmlList, File classPath) throws IOException {
        
        String fileName = classPath.getName();
        if (fileName.toLowerCase().startsWith("surefire")){
            return;
        }
        if (classPath.getAbsolutePath().endsWith(".war!/WEB-INF/classes")) {
           classPath = new File(classPath.getAbsolutePath().substring(0, classPath.getAbsolutePath().lastIndexOf('!')));
        }
        JarFile module = null;
        try {
            module = new JarFile(classPath);
            ZipEntry entry = module.getEntry(searchFor);
            if (entry != null){
                InputStream is = module.getInputStream(entry); 
                processInputStream(xmlList, is);
            }
        } catch (Exception e) {
            logger.info("Unable to check jar file "+fileName+" for ebean-orm.xml");
        } finally {
            if (module != null){
                module.close();
            }
        }
    }

    
}
