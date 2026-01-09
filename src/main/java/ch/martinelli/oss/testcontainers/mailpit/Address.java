package ch.martinelli.oss.testcontainers.mailpit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an email address with optional display name.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Address(@JsonProperty("Name") String name, @JsonProperty("Address") String address) {

	@Override
	public String toString() {
		if (name != null && !name.isEmpty()) {
			return name + " <" + address + ">";
		}
		return address;
	}

}
