package mcjty.incontrol.rules.support;

import java.util.ArrayList;
import java.util.Random;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.math.MathHelper;

public class AttributesScalerMap {

    protected final Logger logger;
    protected Random random;
    public int EntityLevel = 1;
    public float HealthMultiplyer = 1;
    public float SpeedMultiplyer = 1;
    public float DamageMultiplyer = 1;
    private int WeightRange = 0;

    public AttributesScalerMap(Logger logger, Random random) {
        this.logger = logger;
        this.random = random;

    }

    private static ArrayList<Integer> JsonArrayToIntArray(JsonElement json) {

        ArrayList<Integer> newArray = new ArrayList<Integer>();
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonPrimitive()) {
                    newArray.add(array.get(i).getAsInt());
                }
            }
        }
        return newArray;
    }

    public AttributesScalerMap(JsonObject attributes, Logger logger, Random random) {
        this(logger, random);
        if (attributes.has("scale")) {
            JsonElement scaleMapElement = attributes.get("scale");
            if (scaleMapElement.isJsonObject()) {
                JsonObject scaleMapObject = scaleMapElement.getAsJsonObject();
                if (scaleMapObject.has("min") && scaleMapObject.has("max")) {
                    JsonElement minElement = scaleMapObject.get("min");
                    JsonElement maxElement = scaleMapObject.get("max");

                    if (minElement.isJsonPrimitive() && maxElement.isJsonPrimitive()) {
                        int min = minElement.getAsInt();
                        int max = maxElement.getAsInt();
                        if (scaleMapObject.has("weight")) {
                            JsonElement weightElement = scaleMapObject.get("weight");
                            ArrayList<Integer> weightedValues = JsonArrayToIntArray(weightElement);
                            ArrayList<Integer> weights = new ArrayList<Integer>();
                            int range = max - min + 1;
                            int selectedRange = 1;
                            if (weightedValues.size() == range) {
                                this.WeightRange=0;
                                for (int i = 0; i < weightedValues.size(); i++) {
                                    Integer weight = weightedValues.get(i);
                                    for (int a = 0; a < weight; a++) {
                                        weights.add(min + i);
                                    }
                                    this.WeightRange+=weight;
                                }
                                selectedRange=this.random.nextInt(this.WeightRange);

                                

                                if (weights.size() > 1) {
                                    this.EntityLevel = weights.get(selectedRange);

                                    
                                } 
                            }else {
                                this.WeightRange=range;
                                selectedRange = this.random.nextInt(this.WeightRange)+1;
                                this.EntityLevel = selectedRange + min;
                            }

                        }
                        this.HealthMultiplyer = attributeMinMaxScaller(attributes, "healthmultiply");
                        this.DamageMultiplyer = attributeMinMaxScaller(attributes, "damagemultiply");
                        this.SpeedMultiplyer = attributeMinMaxScaller(attributes, "speedmultiply");

                    }
                }

            }

        }

    }

    public float attributeMinMaxScaller(JsonObject attributes, String attributeName) {
        if (attributes.has(attributeName)) {
            JsonElement attributeElement = attributes.get(attributeName);
            if (attributeElement.isJsonArray()) {

                JsonArray attributeArray = attributeElement.getAsJsonArray();
                if (attributeArray.size() == this.WeightRange && attributeArray.get(this.EntityLevel - 1).isJsonObject()) {
                    JsonObject healtObject = attributeArray.get(this.EntityLevel - 1).getAsJsonObject();
                    if (healtObject.has("min") && healtObject.has("max")) {
                        double min = healtObject.get("min").getAsDouble();
                        double max = healtObject.get("max").getAsDouble();
                        return (float) MathHelper.lerp(this.random.nextDouble(), min, max);
                    }
                }
            } else if (attributeElement.isJsonObject()) {
                JsonObject attributeObject = attributeElement.getAsJsonObject();
                if (attributeObject.has("scale") && attributeObject.has("start")) {
                    JsonElement scaleElement = attributeObject.get("scale");
                    JsonElement StartElement = attributeObject.get("start");

                    if (scaleElement.isJsonPrimitive() && StartElement.isJsonPrimitive()) {
                        Float scalable=(float)(StartElement.getAsDouble() + this.EntityLevel * scaleElement.getAsDouble());
                        return scalable;
                    }else{
                        logger.log(Level.WARN, attributeName +"=> NAN");
                    }
                }
            }
        }
        return 1f;
    }

    public AttributesScalerMap() {
        this.logger = null;
    }

    public AttributesScalerMap(Logger logger) {
        this(logger, new Random());
    }

    public boolean hasHealthMultiplyer() {
        return this.HealthMultiplyer != 1;
    }

    public boolean hasDamageMultiplyer() {
        return this.DamageMultiplyer != 1;
    }

    public boolean hasSpeedMultiplyer() {
        return this.SpeedMultiplyer != 1;
    }

}
