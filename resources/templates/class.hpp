#ifndef _{{{pkg-name}}}_{{class-name}}_H_
#define _{{{pkg-name}}}_{{class-name}}_H_

{{#imports}}
#include {{{imported-class}}}
{{/imports}}

namespace {{{pkg-name}}} {
  class {{{class-name}}} {
  private:
    {{#fields}}
    {{{field-type}}} {{{uscored-field-name}}};
    {{/fields}}

  public:
    {{{class-name}}}();
    {{{class-name}}}({{#fields}}{{^first}}, {{/first}}{{{field-type}}} {{{uscored-field-name}}}{{/fields}});
    ~{{{class-name}}}();
    {{#fields}}
    {{{field-type}}} get{{{field-name}}}();
    void set{{{field-name}}}({{{field-type}}} {{{field-name}}});
    {{/fields}}
  };
}

#endif // _{{{pkg-name}}}_{{class-name}}_H_
