#ifndef _{{{pkg-name}}}_Util_H_
#define _{{{pkg-name}}}_Util_H_

#include <string>
#include <ctime>

namespace {{pkg-name}} {
  class Util {
  public:
    static std::tm parseDate(const char* date);
  };
}

#endif // _{{{pkg-name}}}_Util_H_
