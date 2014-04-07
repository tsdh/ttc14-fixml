#include "{{{class-name}}}.hpp"

namespace {{{pkg-name}}} {
  {{{class-name}}}::{{{class-name}}}() {
    {{#fields}}
    this->{{{uscored-field-name}}} = {{{field-value-exp}}};
    {{/fields}}
  }

  {{{class-name}}}::{{{class-name}}}({{#fields}}{{^first}}, {{/first}}{{{field-type}}} {{{uscored-field-name}}}{{/fields}}) {
    {{#fields}}
    this->{{{uscored-field-name}}} = {{{uscored-field-name}}};
    {{/fields}}
  }

  {{{class-name}}}::~{{{class-name}}}() {
    {{#fields}}{{#pointer}}delete {{{uscored-field-name}}};
    {{/pointer}}{{#array}}delete[] {{{uscored-field-name}}};
    {{/array}}{{/fields}}
  }
  {{#fields}}

  {{{field-type}}} {{{class-name}}}::get{{{field-name}}} () {
    return {{{uscored-field-name}}};
  }

  void {{{class-name}}}::set{{{field-name}}} ({{{field-type}}} {{{field-name}}}) {
    {{{uscored-field-name}}} = {{{field-name}}};
  }
  {{/fields}}
}
