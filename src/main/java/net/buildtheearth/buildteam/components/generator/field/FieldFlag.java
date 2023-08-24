package net.buildtheearth.buildteam.components.generator.field;

import net.buildtheearth.buildteam.components.generator.Flag;

public enum FieldFlag implements Flag {

    CROP("c"), //Crop
    TYPE("t"), //CropStage
    FENCE("f"), // String blocks
    FENCE_DISTANCE("fd"); // Byte Distance


    private final String flag;


    FieldFlag(String flag){
        this.flag = flag;
    }

    @Override
    public String getFlag() {
        return flag;
    }

    public static FieldFlag byString(String flag){
        for(FieldFlag fieldFlag : FieldFlag.values())
            if(fieldFlag.getFlag().equalsIgnoreCase(flag))
                return fieldFlag;
        return null;

    }
}