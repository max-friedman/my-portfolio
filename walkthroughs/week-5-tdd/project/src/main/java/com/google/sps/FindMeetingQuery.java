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
    Collection<TimeRange> freeTimeRangesWithOptionalAttendee = new ArrayList<TimeRange>();
    int endOptional = 0, endMandatory = 0, FULL_DAY = 24*60;
    
    Collections.sort(sortedEvents, new Comparator<Event>() {
      @Override public int compare(Event e1, Event e2) {
        return TimeRange.ORDER_BY_START.compare(e1.getWhen(), e2.getWhen());
      }
    });
    
    for(Event e: sortedEvents) {
      if(!Collections.disjoint(e.getAttendees(), request.getAttendees())) {
        if(isOverlapping(e, endMandatory, request.getDuration())) {
          freeTimeRanges.add(TimeRange.fromStartEnd(endMandatory, e.getWhen().start(), false));
        }

        if(endMandatory < e.getWhen().end()) {
          endMandatory = e.getWhen().end();
        }
      }

      if(!Collections.disjoint(e.getAttendees(), request.getAttendees()) || !Collections.disjoint(e.getAttendees(), request.getOptionalAttendees())) {
        if(isOverlapping(e, endOptional, request.getDuration())) {
          freeTimeRangesWithOptionalAttendee.add(TimeRange.fromStartEnd(endOptional, e.getWhen().start(), false));
        }

        if(endOptional < e.getWhen().end()) {
          endOptional = e.getWhen().end();
        }
      }

      
    }

    //If last event doesn't go to the end of the day
    if(endMandatory < FULL_DAY && request.getDuration() < FULL_DAY && !request.getAttendees().isEmpty()) {
      freeTimeRanges.add(TimeRange.fromStartEnd(endMandatory, FULL_DAY, false));
    }
    if(endOptional < FULL_DAY && request.getDuration() < FULL_DAY) {
      freeTimeRangesWithOptionalAttendee.add(TimeRange.fromStartEnd(endOptional, FULL_DAY, false));
    }
    
    if(!freeTimeRangesWithOptionalAttendee.isEmpty()) {
      return freeTimeRangesWithOptionalAttendee;
    }
    return freeTimeRanges;
  }

  private boolean isOverlapping(Event e, int end, long duration) {
    if(e.getWhen().start() > end && e.getWhen().start()-duration >= end) {
      return true;
    }
    return false;
  }
}
