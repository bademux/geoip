package com.github.bademux.geoip.api.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ErrorApiDto
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
public class ErrorApiDto   {
  @JsonProperty("id")
  private UUID id;

  @JsonProperty("code")
  private String code;

  @JsonProperty("timestamp")
  @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime timestamp;

  @JsonProperty("message")
  private String message;

  public ErrorApiDto id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Unique error ID, used for logging purposes, UUID format
   * @return id
  */
  @ApiModelProperty(required = true, value = "Unique error ID, used for logging purposes, UUID format")
  @NotNull

  @Valid

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ErrorApiDto code(String code) {
    this.code = code;
    return this;
  }

  /**
   * A string coding the error type. This is given to caller so he can translate them if required.
   * @return code
  */
  @ApiModelProperty(required = true, value = "A string coding the error type. This is given to caller so he can translate them if required.")
  @NotNull


  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public ErrorApiDto timestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Exact time of error
   * @return timestamp
  */
  @ApiModelProperty(required = true, value = "Exact time of error")
  @NotNull

  @Valid

  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public ErrorApiDto message(String message) {
    this.message = message;
    return this;
  }

  /**
   * A short localized string that describes the error.
   * @return message
  */
  @ApiModelProperty(value = "A short localized string that describes the error.")


  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ErrorApiDto error = (ErrorApiDto) o;
    return Objects.equals(this.id, error.id) &&
        Objects.equals(this.code, error.code) &&
        Objects.equals(this.timestamp, error.timestamp) &&
        Objects.equals(this.message, error.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, code, timestamp, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorApiDto {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

