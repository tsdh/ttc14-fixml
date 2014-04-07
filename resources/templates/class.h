#ifndef _{{class-name}}_H_
#define _{{class-name}}_H_

{{#imports}}
#include {{{imported-class}}}
{{/imports}}

typedef struct {
  {{#fields}}
  {{{field-type}}} {{{field-name}}};
  {{/fields}}
} {{{class-name}}};

{{{class-name}}}* make_default_{{{class-name}}}();

{{{class-name}}}* make_{{{class-name}}}({{#fields}}{{^first}}, {{/first}}{{{field-type}}} {{{uscored-field-name}}}{{/fields}});

void free_{{{class-name}}}({{{class-name}}}* x);

#endif // _{{class-name}}_H_
