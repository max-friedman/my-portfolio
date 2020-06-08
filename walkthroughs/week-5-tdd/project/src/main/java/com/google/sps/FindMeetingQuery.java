// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;

public final class FindMeetingQuery {
  int FULL_DAY = 24*60;
  Boolean[] freeStartTimes = new Boolean[FULL_DAY];

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    initializeFreeStartTimes();

    for(Event e: events) {
      if(!Collections.disjoint(e.getAttendees(), request.getAttendees())) {
        busy(e, (int)request.getDuration());
      }
    }

    return createFreeTimeRanges((int)request.getDuration());
  }

  private void initializeFreeStartTimes() {
    Arrays.fill(freeStartTimes, Boolean.TRUE);
  }

  private void busy(Event e, int duration) {
    for(int i = (e.getWhen().start()-duration); i < e.getWhen().end(); ++i) {
      if(i < 0) {
        i = 0;
      }
      if(i==(8*60+30)) {
        System.out.println(e.getTitle());
      }
      freeStartTimes[i] = false;
    }
  }

  private Collection<TimeRange> createFreeTimeRanges(int duration) {
    Collection<TimeRange> freeTimeRanges = new ArrayList<TimeRange>();

    if(duration > FULL_DAY) {
      return freeTimeRanges;
    }

    boolean current = false;
    int start = -1, end = -1;

    for(int i = 0; i < FULL_DAY; ++i) {
      if(freeStartTimes[i]) {
        if(!current) {
          start = i;
          current = true;
        }
        end = i;
      } else if(current == true) {
        freeTimeRanges.add(TimeRange.fromStartEnd(start, end+duration, true));
        current = false;
      }
    }
    //printFreeStartTimes();
    if(current == true) {
      freeTimeRanges.add(TimeRange.fromStartEnd(start, end, true));
    }
    return freeTimeRanges;
  }

  public void printFreeStartTimes() {
    for(int i = 0; i < FULL_DAY; ++i) {
      System.out.print(i + ": ");
      if(freeStartTimes[i]) {
        System.out.println("TRUE");
      } else {
        System.out.println("FALSE");
      }
    }
  }
}
