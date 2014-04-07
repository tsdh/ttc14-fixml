#include "Util.h"
#include <stdarg.h>
#include <stdlib.h>

void** make_pointer_array(int size, ...) {
  va_list ap;
  va_start(ap, size);
  void** ary = malloc(sizeof(void*) * size + 1);
  int i;
  for (i = 0; i < size; i++) {
    ary[i] = va_arg(ap, void*);
  }
  ary[i] = NULL;
  va_end(ap);
  return ary;
}

struct tm parseDate(const char* date) {
  struct tm tmp;
  strptime(date, "%FT%TZ", &tmp);
  return tmp;
}
