package com.avaje.tests.compositekeys;

import com.avaje.ebean.config.dbplatform.H2Platform;

public class ModifiedH2Platform extends H2Platform {

    public ModifiedH2Platform() {
        super();
        this.idInExpandedForm = true;
    }
}
