package workWithAnnotations;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DiAnnotation {
    private final Annotation type;
    private final Map<String, Object> attributes;

    public DiAnnotation(Annotation type, Map<String, Object> attributes){
        this.type = type;
        this.attributes = attributes;
    }
    public Annotation getType() {
        return type;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
    public Object getAttributeValue(String key) {
        return getAttributeAs(key, Object.class);
    }

    public <T> T getAttributeAs(String key, Class<T> type) {
        Optional<Object> attribute = Optional.ofNullable(attributes.get(key));
        if (attribute.isPresent()) {
            Object obj = attribute.get();
            if (type.isAssignableFrom(obj.getClass())) {
                return type.cast(obj);
            } else {
                throw new IllegalStateException("Attribute " + key + " is type " + obj.getClass() + " but " + type + " is expected");
            }
        } else {
            throw new IllegalArgumentException("Attribute " + key + " cannot be found on " + type);
        }
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof DiAnnotation castOther)) {
            return false;
        }
        return Objects.equals(type, castOther.type) && Objects.equals(attributes, castOther.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, attributes);
    }

    @Override
    public String toString() {
        return "DiAnnotation [type=" + type + ", attributes=" + attributes + "]";
    }

}
