package com.us.traystorage.data.model;

import java.util.List;

public class ModelCategory extends ModelBase {
    public int id;
    public int user_id;
    public String name;
    public Integer color;
    public String icon;
    public Integer sort_order;
    public String create_time;
    public String update_time;
    
    // 카테고리 ??문서 ??(?�버?�서 계산?�서 ?�려�?
    public int document_count;

    public ModelCategory() {
    }

    public ModelCategory(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static class ListModel extends ModelBase {
        public List<ModelCategory> category_list;
    }

    public static class DetailModel extends ModelBase {
        public ModelCategory category;
    }
}
