package com.avaje.tests.basic;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.PFile;
import com.avaje.tests.model.basic.PFileContent;

import junit.framework.TestCase;

public class TestDeleteImportedPartial extends TestCase {
 
    public void test() {
    

        PFile persistentFile = new PFile("test.txt", new PFileContent("test".getBytes()));

        Ebean.save(persistentFile);
        Integer id = persistentFile.getId();
        Integer contentId = persistentFile.getFileContent().getId();

        PFile partialPfile = Ebean.find(PFile.class)
            .select("id")
            .where().idEq(persistentFile.getId())
            .findUnique();
        
        // should delete file and fileContent
        Ebean.delete(partialPfile);
        System.out.println("finished delete");

        PFile file1 = Ebean.find(PFile.class, id);
        PFileContent content1 = Ebean.find(PFileContent.class, contentId);

        Assert.assertNull(file1);
        Assert.assertNull(content1);        
        
    }
}
