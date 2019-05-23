/*
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.kork.web.selector;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.util.Assert;

public class SelectableService {
  private final List<ServiceSelector> serviceSelectors;

  public SelectableService(List<ServiceSelector> serviceSelectors) {
    this.serviceSelectors = serviceSelectors;
  }

  public Object getService(Criteria criteria) {
    Assert.notNull(criteria);

    return serviceSelectors.stream()
        .filter(it -> it.supports(criteria))
        .sorted((a, b) -> b.getPriority() - a.getPriority())
        .findFirst()
        .map(ServiceSelector::getService)
        .orElse(serviceSelectors.get(0).getService());
  }

  public static class Criteria {
    private String application;
    private String authenticatedUser;
    private String executionType;
    private String executionId;
    private String origin;
    private String location;
    private List<Parameter> parameters;

    public String getApplication() {
      return application;
    }

    public String getAuthenticatedUser() {
      return authenticatedUser;
    }

    public String getExecutionType() {
      return executionType;
    }

    public String getExecutionId() {
      return executionId;
    }

    public String getOrigin() {
      return origin;
    }

    public String getLocation() {
      return location;
    }

    public List<Parameter> getParameters() {
      return parameters;
    }

    public Criteria withApplication(String application) {
      this.application = application;
      return this;
    }

    public Criteria withAuthenticatedUser(String user) {
      this.authenticatedUser = user;
      return this;
    }

    public Criteria withOrigin(String origin) {
      this.origin = origin;
      return this;
    }

    public Criteria withExecutionType(String executionType) {
      this.executionType = executionType;
      return this;
    }

    public Criteria withExecutionId(String executionId) {
      this.executionId = executionId;
      return this;
    }

    public Criteria withLocation(String location) {
      this.location = location;
      return this;
    }

    public Criteria withParameters(List<Parameter> parameters) {
      this.parameters = parameters;
      return this;
    }

    static class Parameter {
      private String name;
      private List<Object> values;

      Parameter(Map<String, Object> source) {
        this.name = source.get("name").toString();
        this.values = (List<Object>) source.get("values");
      }

      public Parameter(String name, List<Object> values) {
        this.name = name;
        this.values = values;
      }

      public Parameter withName(String name) {
        this.setName(name);
        return this;
      }

      public Parameter withValues(List<Object> values) {
        this.setValues(values);
        return this;
      }

      public String getName() {
        return name;
      }

      public void setName(String name) {
        this.name = name;
      }

      public List<Object> getValues() {
        return values;
      }

      public void setValues(List<Object> values) {
        this.values = values;
      }

      static List<Parameter> toParameters(List<Map<String, Object>> source) {
        return source.stream().map(Parameter::new).collect(Collectors.toList());
      }

      @Override
      public int hashCode() {
        return Objects.hash(name, values);
      }

      @Override
      public boolean equals(Object o) {
        if (o instanceof Parameter) {
          Parameter other = (Parameter) o;
          if (!this.name.equals(other.getName())) {
            return false;
          }

          for (Object v : this.values) {
            if (v instanceof String && ((String) v).startsWith("regex:")) {
              final String regex = ((String) v).split(":")[1];
              if (other.getValues().stream().anyMatch(i -> ((String) i).matches(regex))) {
                return true;
              }
            } else {
              if (other.getValues().stream().anyMatch(i -> i.equals(v))) {
                return true;
              }
            }
          }
        }

        return false;
      }
    }
  }
}
