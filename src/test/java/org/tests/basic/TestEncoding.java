/*
 * Licensed Materials - Property of FOCONIS AG
 * (C) Copyright FOCONIS AG.
 */

package org.tests.basic;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.tests.model.basic.EBasic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;

/**
 * Tests various encoding issues with different platforms
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class TestEncoding extends BaseTestCase {

  /**
   * Checks, if the search works exactly. It must be case sensitive and
   * distinguish between Umlauts and their paraphrase ( ä!=ae, ß!=ss ).
   * @throws SQLException
   */
  @Test
  public void testStringFind() throws SQLException {

    // prepare tests
    Collection<String> testData = Arrays.asList("Übel", "Uebel", "übel", "uebel", "Weiss", "Weiß", "Bär", "Baer", "A",
        "a", "Ä", "ä", "€µè§²³áàâ", // special chars
        "ÄÖÜäöüß", // Umlauts
        "АБВ", // Cyrillic
        "\uD800\uDF00\uD800\uDF01\uD800\uDF02", // old italic
        "ÄB̈C̈", // Diacritic
        "\uD83D\uDE08\uD83D\uDCA9", // Emoticons

        // 2 byte unicode
        "ÉÊËÌÍÎÏÐÑÒÓ ŰÝÞßàáâãäå ăæçèéêëìíî ðñòóôõöőøș αβγδθικλμν ΓΔΕΖΗΘΙΚΛΜ ΝΞΟΠΡΣΤΥΦΧ ظ ع غ ف ق ك ل م ن ه",

        // 3 byte unicode https://gist.github.com/jimjam88/cce5b57c50c973c313de
        "ルビンツアウェブふべ|からずセシリテどトモ|デプロファイとマィッ|めよう内准剛んを始コ|展久スド情報して使|にほるレでのすソた「",

        // 4 byte unicode
        "𠜎𠜱𠝹𠱓𠱸𠲖𠳏𠳕𠴕𠵼 𠵿𠸎𠸏𠹷𠺝𠺢𠻗𠻹𠻺𠼭 𠼮𠽌𠾴𠾼𠿪𡁜𡁯𡁵𡁶𡁻𡃁 𡃉𡇙𢃇𢞵𢫕𢭃𢯊𢱑𢱕𢳂𢴈");

    List<Integer> ids = new ArrayList<>();
    // create test entries
    for (String entry : testData) {
      System.out.println("Inserting: " + entry);
      EBasic entity = new EBasic();
      entity.setName(entry);
      Ebean.save(entity);
    }


    for (String entry : testData) {
      System.out.println("Searching: " + entry);
      //try (Transaction txn = Ebean.beginTransaction()) {
//        Statement stmt = txn.getConnection().createStatement();
//        ResultSet rset = stmt.executeQuery("show variables WHERE variable_name like \"col%\" ");
//        while (rset.next()) {
//          System.out.println(rset.getString(1)+": " + rset.getString(2));
//        }
      EBasic current = Ebean.find(EBasic.class).where().eq("name", entry).findOne();
      assertThat(current.getName()).isEqualTo(entry);

      current = Ebean.find(EBasic.class).where().in("name", entry).findOne();
      assertThat(current.getName()).isEqualTo(entry);


      current = Ebean.find(EBasic.class).where().in("name", entry, "gibtsned").findOne();
      assertThat(current.getName()).isEqualTo(entry);
      //}
    }

    // For MySQL to debug:
    //    try (Transaction txn = Ebean.beginTransaction()) {
    //      Statement stmt = txn.getConnection().createStatement();
    //      ResultSet rset = stmt.executeQuery("show variables WHERE variable_name like \"col%\" ");
    //      while (rset.next()) {
    //        System.out.println(rset.getString(1)+": " + rset.getString(2));
    //      }
    //    }

    List<EBasic> list = Ebean.find(EBasic.class).where().in("name", testData).findList();
    assertThat(list.size()).isEqualTo(testData.size());
    for (EBasic current : list) {
      assertThat(testData).contains(current.getName());
    }
  }

}
