#include "Util.hpp"

namespace {{pkg-name}} { 
  std::tm Util::parseDate(const char* date) {
    std::tm tmp;
    strptime(date, "%FT%TZ", &tmp);
    return tmp;
  }
}
