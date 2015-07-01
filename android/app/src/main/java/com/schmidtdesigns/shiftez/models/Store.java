package com.schmidtdesigns.shiftez;

import com.google.gson.annotations.Expose;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class Store {

    @Expose
    private List<Store_> stores = new ArrayList<Store_>();

    /**
     *
     * @return
     * The stores
     */
    public List<Store_> getStores() {
        return stores;
    }

    /**
     *
     * @param stores
     * The stores
     */
    public void setStores(List<Store_> stores) {
        this.stores = stores;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

        package com.schmidtdesigns.shiftez;

        import java.util.ArrayList;
        import java.util.List;
        import javax.annotation.Generated;
        import com.google.gson.annotations.Expose;
        import com.google.gson.annotations.SerializedName;
        import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class Store_ {

    @Expose
    private List<String> deps = new ArrayList<String>();
    @Expose
    private String store;
    @SerializedName("user_id")
    @Expose
    private String userId;

    /**
     *
     * @return
     * The deps
     */
    public List<String> getDeps() {
        return deps;
    }

    /**
     *
     * @param deps
     * The deps
     */
    public void setDeps(List<String> deps) {
        this.deps = deps;
    }

    /**
     *
     * @return
     * The store
     */
    public String getStore() {
        return store;
    }

    /**
     *
     * @param store
     * The store
     */
    public void setStore(String store) {
        this.store = store;
    }

    /**
     *
     * @return
     * The userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     *
     * @param userId
     * The user_id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}