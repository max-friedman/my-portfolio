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
import java.util.Comparator;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<Event> sortedEvents = new ArrayList<Event>(events);
    Collection<TimeRange> freeTimeRanges = new ArrayList<TimeRange>();
    Collection<TimeRange> freeTimeRangesOptional = new ArrayList<TimeRange>();
    int end = 0, FULL_DAY = 24*60, endMandatory = 0;
    
    Collections.sort(sortedEvents, new Comparator<Event>() {
      @Override public int compare(Event e1, Event e2) {
        return TimeRange.ORDER_BY_START.compare(e1.getWhen(), e2.getWhen());
      }
    });
    
    for(Event e: sortedEvents) {
      if(!Collections.disjoint(e.getAttendees(), request.getAttendees())) {
        if(e.getWhen().start() > end && e.getWhen().start()-request.getDuration() >= end) {
          freeTimeRanges.add(TimeRange.fromStartEnd(end, e.getWhen().start(), false));
        }

        if(end < e.getWhen().end()) {
          end = e.getWhen().end();
        }
      }
    }

    //If last event doesn't go to the end of the day
    if(end < FULL_DAY && request.getDuration() < FULL_DAY && !request.getAttendees().isEmpty()) {
      freeTimeRanges.add(TimeRange.fromStartEnd(end, FULL_DAY, false));
    }

    // Duplicated for optional
    end = 0;
    for(Event e: sortedEvents) {
      if(!Collections.disjoint(e.getAttendees(), request.getAttendees()) || !Collections.disjoint(e.getAttendees(), request.getOptionalAttendees())) {
        if(e.getWhen().start() > end && e.getWhen().start()-request.getDuration() >= end) {
          freeTimeRangesOptional.add(TimeRange.fromStartEnd(end, e.getWhen().start(), false));
        }

        if(end < e.getWhen().end()) {
          end = e.getWhen().end();
        }
      }
    }

    //If last event doesn't go to the end of the day
    if(end < FULL_DAY && request.getDuration() < FULL_DAY) {
      freeTimeRangesOptional.add(TimeRange.fromStartEnd(end, FULL_DAY, false));
    }
    //
    
    if(!freeTimeRangesOptional.isEmpty()) {
      return freeTimeRangesOptional;
    }
    return freeTimeRanges;
  }
}
