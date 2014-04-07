{{#imports}}
using {{{imported-class}}};
{{/imports}}

namespace {{{pkg-name}}}{
  class {{{class-name}}} {
    {{#fields}}
    public {{{field-type}}} {{{uscored-field-name}}} { get; set; }
    {{/fields}}

    public {{{class-name}}}() {
      {{#fields}}
      this.{{{uscored-field-name}}} = {{{field-value-exp}}};
      {{/fields}}
    }

    public {{{class-name}}}({{#fields}}{{^first}}, {{/first}}{{{field-type}}} {{{field-name}}}{{/fields}}) {
      {{#fields}}
      this.{{{uscored-field-name}}} = {{{field-name}}};
      {{/fields}}
    }
  }
}
