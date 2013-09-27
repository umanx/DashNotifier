package com.umang.dashnotifier;

import android.graphics.drawable.Drawable;

public class PackageItem {

    private Drawable icon;
    private String name;
    private String packageName;
    
    public PackageItem(){
    	
    }
    
    public PackageItem(String packageName, String appName, Drawable appIcon){
    	this.icon = appIcon;
    	this.name = appName;
    	this.packageName = packageName;
    
    }
    
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    
    public void setAll(String packageName, String appName, Drawable appIcon){
    	this.icon = appIcon;
    	this.name = appName;
    	this.packageName = packageName;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
    
}