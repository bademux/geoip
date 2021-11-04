package com.github.bademux.geoip.api.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * NorthcountriesApiDto
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
public class NorthcountriesApiDto   {
  @JsonProperty("northcountries")
  @Valid
  private List<String> northcountries = new ArrayList<>();

  public NorthcountriesApiDto northcountries(List<String> northcountries) {
    this.northcountries = northcountries;
    return this;
  }

  public NorthcountriesApiDto addNorthcountriesItem(String northcountriesItem) {
    if (this.northcountries == null) {
      this.northcountries = new ArrayList<>();
    }
    this.northcountries.add(northcountriesItem);
    return this;
  }

  /**
   * Get northcountries
   * @return northcountries
  */
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public List<String> getNorthcountries() {
    return northcountries;
  }

  public void setNorthcountries(List<String> northcountries) {
    this.northcountries = northcountries;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NorthcountriesApiDto northcountries = (NorthcountriesApiDto) o;
    return Objects.equals(this.northcountries, northcountries.northcountries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(northcountries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NorthcountriesApiDto {\n");
    
    sb.append("    northcountries: ").append(toIndentedString(northcountries)).append("\n");
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

