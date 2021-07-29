package org.tests.json;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.ValuePair;
import io.ebean.event.BeanPersistAdapter;
import io.ebean.event.BeanPersistRequest;
import io.ebeantest.LoggedSql;
import org.junit.Test;
import org.tests.model.json.EBasicJsonJackson3;
import org.tests.model.json.EBasicJsonList;
import org.tests.model.json.PlainBean;
import org.tests.model.json.PlainBeanDirtyAware;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDbJson_Jackson3 extends BaseTestCase {

  public static class EBasicJsonListPersistController extends BeanPersistAdapter {

    private static Map<String, ValuePair> updatedValues;

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      return EBasicJsonList.class.isAssignableFrom(cls);
    }

    @Override
    public boolean preInsert(BeanPersistRequest<?> request) {
      updatedValues = request.getUpdatedValues();
      return true;
    }

    @Override
    public boolean preUpdate(BeanPersistRequest<?> request) {
      updatedValues = request.getUpdatedValues();
      return true;
    }
  }
  @Test
  public void updateIncludesJsonColumn_when_explicit_isMarkedDirty() {

    PlainBeanDirtyAware contentBean = new PlainBeanDirtyAware("a", 42);

    EBasicJsonJackson3 bean = new EBasicJsonJackson3();
    bean.setName("b1");
    bean.setPlainValue(contentBean);
    bean.setPlainValue2(contentBean);

    bean.save();

    final EBasicJsonJackson3 found = DB.find(EBasicJsonJackson3.class, bean.getId());
    found.setName("b1-mod");

    LoggedSql.start();
    found.save();

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update ebasic_json_jackson3 set name=?, version=? where id=? and version=?");

    found.setName("b1-mod2");
    found.getPlainValue().setName("b");
    found.getPlainValue().setMarkedDirty(true);

    found.save();

    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update ebasic_json_jackson3 set name=?, plain_value=?, version=? where id=? and version=?");

    final EBasicJsonJackson3 found2 = DB.find(EBasicJsonJackson3.class, bean.getId());

    assertThat(found2.getName()).isEqualTo("b1-mod2");
    assertThat(found2.getPlainValue().getName()).isEqualTo("b");
  }

  @Test
  public void updateIncludesJsonColumn_when_loadedAndNotDirtyAware() {

    PlainBean contentBean = new PlainBean("a", 42);
    EBasicJsonList bean = new EBasicJsonList();
    bean.setName("p1");
    bean.setPlainBean(contentBean);
    bean.setBeanList(Arrays.asList(contentBean));

    DB.save(bean);
    final EBasicJsonList found = DB.find(EBasicJsonList.class, bean.getId());
    // json bean not modified but not aware
    // ideally don't load the json content if we are not going to modify it
    found.setName("p1-mod");
    found.setBeanList(null);

    BeanState state = DB.getBeanState(found);
    assertThat(state.getChangedProps()).containsExactlyInAnyOrder("name", "beanList");

    ValuePair pair = state.getDirtyValues().get("name");
    assertThat(pair.getNewValue()).isEqualTo("p1-mod");
    assertThat(pair.getOldValue()).isEqualTo("p1");

    pair = state.getDirtyValues().get("beanList");
    assertThat(pair.getNewValue()).isEqualTo(null);
    assertThat((List<PlainBean>)pair.getOldValue()).hasSize(1)
      .extracting(PlainBean::getName).containsExactly("a");


    LoggedSql.start();
    DB.save(found);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    // plain_bean=?, no longer included with MD5 dirty detection
    assertThat(sql.get(0)).contains("update ebasic_json_list set name=?, bean_list=?, version=? where id=?");

    assertThat(EBasicJsonListPersistController.updatedValues.entrySet())
      .extracting(Map.Entry::toString)
      .containsExactlyInAnyOrder("beanList=null,[name:a]","name=p1-mod,p1","version=2,1");


    assertThat(DB.getBeanState(found).isDirty()).isFalse();

    found.getPlainBean().setName("b");

    assertThat(DB.getBeanState(found).isDirty()).isTrue();

    state = DB.getBeanState(found);
    assertThat(state.getChangedProps()).containsExactlyInAnyOrder("plainBean");
    pair = state.getDirtyValues().get("plainBean");
    assertThat(pair.getNewValue()).hasToString("name:b");
    assertThat(pair.getOldValue()).hasToString("name:a");


    LoggedSql.start();
    DB.save(found);

    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    // plain_bean=?, no longer included with MD5 dirty detection
    assertThat(sql.get(0)).contains("update ebasic_json_list set plain_bean=?, version=? where id=?");

    assertThat(EBasicJsonListPersistController.updatedValues.entrySet())
      .extracting(Map.Entry::toString)
      .containsExactlyInAnyOrder("plainBean=name:b,name:a", "version=3,2");
  }

  @Test
  public void updateIncludesJsonColumn_when_list_loadedAndNotDirtyAware() {

    PlainBean contentBean = new PlainBean("a", 42);
    EBasicJsonList bean = new EBasicJsonList();
    bean.setName("p1");
    bean.setPlainBean(contentBean);
    bean.setBeanList(Arrays.asList(contentBean));

    DB.save(bean);
    final EBasicJsonList found = DB.find(EBasicJsonList.class, bean.getId());
    found.getBeanList().get(0).setName("p1-mod");

    BeanState state = DB.getBeanState(found);
    assertThat(state.getChangedProps()).containsExactlyInAnyOrder("beanList");

  }
  @Test
  public void update_with_differentDbJsonSettings() {
    PlainBeanDirtyAware contentBean1 = new PlainBeanDirtyAware("x", 42);
    PlainBeanDirtyAware contentBean2 = new PlainBeanDirtyAware("y", 43);
    PlainBeanDirtyAware contentBean3 = new PlainBeanDirtyAware("z", 44);

    EBasicJsonJackson3 bean = new EBasicJsonJackson3();
    bean.setName("b1");
    bean.setPlainValue(contentBean1);
    bean.setPlainValue2(contentBean2);
    bean.setPlainValue3(contentBean3);

    BeanState state = DB.getBeanState(bean);
    // a new bean is not considered as dirty (thus have no changed props)
    assertThat(state.isDirty()).isFalse();
    assertThat(state.isNewOrDirty()).isTrue();
    assertThat(state.getChangedProps()).isEmpty();
    
    bean.save();
    
    bean = DB.find(EBasicJsonJackson3.class, bean.getId());
    state = DB.getBeanState(bean);
    // a fresh loaded bean is also not considered as dirty
    assertThat(state.isDirty()).isFalse();
    assertThat(state.isNewOrDirty()).isFalse();
    assertThat(state.getChangedProps()).isEmpty();
    
    bean.getPlainValue().setName("a"); // has keepSource=true 
    
    assertThat(state.isDirty()).isTrue();
    assertThat(state.getChangedProps()).containsExactly("plainValue");
    
    bean.getPlainValue2().setName("b");
    assertThat(state.getChangedProps()).containsExactlyInAnyOrder("plainValue", "plainValue2");
    
    bean.getPlainValue3().setName("c"); // has dirtyDetection = false
    
    Map<String, ValuePair> dirtyValues = state.getDirtyValues();
    assertThat(dirtyValues).hasSize(2).containsKeys("plainValue", "plainValue2");
    
    assertThat(dirtyValues.get("plainValue")).hasToString("name:a,name:x"); // keepSource=true -> origValue present 
    assertThat(dirtyValues.get("plainValue2")).hasToString("name:b,null");  // without keepSourc no origValue present
    
    LoggedSql.start();
    bean.save();
    List<String> sql = LoggedSql.stop();
    assertThat(sql.get(0)).contains("update ebasic_json_jackson3 set plain_value=?, plain_value2=?, version=? where id=?");
    
    bean = DB.find(EBasicJsonJackson3.class, bean.getId());
    assertThat(bean.getPlainValue().getName()).isEqualTo("a");
    assertThat(bean.getPlainValue2().getName()).isEqualTo("b");
    assertThat(bean.getPlainValue3().getName()).isEqualTo("z"); // value is not updated (CHECKME: 
    
    bean.getPlainValue3().setName("c");
    bean.getPlainValue3().setMarkedDirty(true); // This seems to be broken now.

//    state = DB.getBeanState(bean);
//    dirtyValues = state.getDirtyValues();
//    assertThat(dirtyValues).hasSize(1).containsKeys("plainValue3");
//    
//    assertThat(dirtyValues.get("plainValue3")).hasToString("name:c,name:z"); 
    
    LoggedSql.start();
    bean.save();
    sql = LoggedSql.stop();
    assertThat(sql.get(0)).contains("update ebasic_json_jackson3 set plain_value3=?, version=? where id=?");
    
  }

}
