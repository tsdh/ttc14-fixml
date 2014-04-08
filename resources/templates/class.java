package {{{pkg-name}}};

{{#imports}}
import {{{imported-class}}};
{{/imports}}

class {{{class-name}}} {
    {{#fields}}
    private {{{field-type}}} {{{field-name}}};
    {{/fields}}

    public {{{class-name}}}() {
	{{#fields}}
	this.{{{field-name}}} = {{{field-value-exp}}};
	{{/fields}}
    }

    public {{{class-name}}}({{#fields}}{{^first}}, {{/first}}{{{field-type}}} {{{field-name}}}{{/fields}}) {
	{{#fields}}
	this.{{{field-name}}} = {{{field-name}}};
	{{/fields}}
    }
    {{#fields}}

    public {{{field-type}}} get{{{field-name}}}() {
	return {{{field-name}}};
    }

    public void set{{{field-name}}}({{{field-type}}} {{{field-name}}}) {
	this.{{{field-name}}} = {{{field-name}}};
    }
    {{/fields}}
}
