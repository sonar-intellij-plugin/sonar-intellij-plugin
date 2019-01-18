package org.intellij.sonar.sonarreport;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.joda.time.DateTime;

public class DateTimeTypeConverter
  implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

  @Override
  public JsonElement serialize(DateTime src,Type srcType,JsonSerializationContext context) {
    return new JsonPrimitive(src.toString());
  }

  @Override
  public DateTime deserialize(JsonElement json,Type type,JsonDeserializationContext context) {
    try {
      return new DateTime(json.getAsString());
    } catch (IllegalArgumentException ignore) { //NOSONAR
      // May be it came in formatted as a java.util.Date, so try that
      Date date = context.deserialize(json,Date.class);
      return new DateTime(date);
    }
  }
}
