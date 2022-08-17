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
    public int weight = 1;
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
        logger.log(Level.INFO, "atttributes => " + attributes.toString());
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
                        this.WeightRange = max - min + 1;
                        int selectedRange = this.random.nextInt(this.WeightRange - 1);

                        if (scaleMapObject.has("weight")) {
                            JsonElement weightElement = scaleMapObject.get("weight");
                            ArrayList<Integer> weightedValues = JsonArrayToIntArray(weightElement);
                            ArrayList<Integer> weights = new ArrayList<Integer>();
                            logger.log(Level.INFO, "weightedValues :" + weightedValues.toString());
                            if (weightedValues.size() == this.WeightRange) {
                                for (int i = 0; i < weightedValues.size(); i++) {
                                    Integer weight = weightedValues.get(i);
                                    for (int a = 0; a < weight; a++) {
                                        weights.add(i + 1);
                                    }
                                }

                                logger.log(Level.INFO, "weights" + weights.toString());
                            }

                            if (weights.size() > 1) {
                                this.weight = weights.get(selectedRange) + min;
                            } else {
                                this.weight = selectedRange + min;
                            }

                            logger.log(Level.INFO, weight);
                        }
                        this.HealthMultiplyer = attributeMinMaxScaller(attributes, "healthmultiply");
                        this.DamageMultiplyer = attributeMinMaxScaller(attributes, "damagemultiply");
                        this.SpeedMultiplyer = attributeMinMaxScaller(attributes, "speedmultiply");
                        logger.log(Level.INFO,
                                this.HealthMultiplyer + "," + this.DamageMultiplyer + "," + this.SpeedMultiplyer);

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
                if (attributeArray.size() == this.WeightRange && attributeArray.get(this.weight - 1).isJsonObject()) {
                    JsonObject healtObject = attributeArray.get(this.weight - 1).getAsJsonObject();
                    if (healtObject.has("min") && healtObject.has("max")) {
                        double min = healtObject.get("min").getAsDouble();
                        double max = healtObject.get("max").getAsDouble();
                        logger.log(Level.INFO, attributeName + " min,max = " + min + " " + max);
                        return (float) MathHelper.lerp(this.random.nextDouble(), min, max);
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
