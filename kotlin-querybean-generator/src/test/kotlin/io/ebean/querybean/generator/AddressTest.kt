package io.ebean.querybean.generator

import org.example.domain.Address
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class AddressTest {

//  private val fieldsInQueryBean = javaClass.classLoader.loadClass("org.example.domain.query.QAddress")
//    ?.declaredFields
//    ?: fail()
  private val fieldsInBean = Address::class.java.declaredFields

  @Test
  fun `assert no transient fields`() {
    assertFieldsNotExists("javaxPersistenceTransient")
    assertFieldsNotExists("kotlinJvmTransient")
  }

  private fun assertFieldsNotExists(fieldName: String) {
    assertTrue(fieldsInBean.any { it.name == fieldName }) {
      "$fieldName does not exist in Address."
    }
//    assertTrue(fieldsInQueryBean.none { it.name == fieldName}) {
//      "$fieldName does exists in query bean for Address (QAddress)."
//    }
  }
}
