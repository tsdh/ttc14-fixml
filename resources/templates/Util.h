#ifndef _Util_H_
#define _Util_H_

#include <time.h>

void** make_pointer_array(int size, ...);
struct tm parseDate(const char* date);

#endif // _Util_H_
