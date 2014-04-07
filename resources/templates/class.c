#include "{{{class-name}}}.h"
#include <stdlib.h>

{{{class-name}}}* make_default_{{{class-name}}}() {
  {{{class-name}}}* tmp = malloc(sizeof({{{class-name}}}));
  {{#fields}}
  tmp->{{{field-name}}} = {{{field-value-exp}}};
  {{/fields}}
  return tmp;
}

{{{class-name}}}* make_{{{class-name}}}({{#fields}}{{^first}}, {{/first}}{{{field-type}}} {{{uscored-field-name}}}{{/fields}}) {
  {{{class-name}}}* tmp = malloc(sizeof({{{class-name}}}));
  {{#fields}}
  tmp->{{{field-name}}} = {{{uscored-field-name}}};
  {{/fields}}
  return tmp;
}

void free_{{{class-name}}}({{{class-name}}}* sp) {
  {{#fields}}
  {{#pointer}}
  free_{{{plain-field-type}}}(sp->{{{field-name}}});
{{/pointer}}{{#array}}
  {{{plain-field-type}}}* tmp_{{field-name}} = *sp->{{{field-name}}};
  while (tmp_{{field-name}} != NULL) {
    free_{{{plain-field-type}}}(tmp_{{field-name}});
    tmp_{{field-name}}++;
  }
  free(sp->{{{field-name}}});
{{/array}}{{/fields}}
  free(sp);
}
