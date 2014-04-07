using System;
using System.Globalization;

namespace {{{pkg-name}}} {
  class Util {
    public static DateTime parseDate(string date) {
      return DateTime.Parse(date,  null, DateTimeStyles.RoundtripKind);
    }
  }
}
