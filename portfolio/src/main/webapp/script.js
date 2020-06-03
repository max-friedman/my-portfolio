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

var i = 0;
var txt = 'Hi, my name is Max./Welcome to my portfolio.';
var speed = 50;

function typewriter() {
  if (i < txt.length) {
    if(txt.charAt(i) === "/") {
      document.getElementById("greeting").innerHTML += '<br>';
    } else {
      document.getElementById("greeting").innerHTML += txt.charAt(i);
    }
    ++i;
    setTimeout(typewriter, speed);
  } else {
    fadeIn('header');
    fadeIn('header-container');
  }
}

var cursorAppear = true;
function cursorBlink() {    
  if (cursorAppear) {
    document.getElementById("cursor").style.color = "#839496";
  } else {
    document.getElementById("cursor").style.color = "#002b36";
  }
  cursorAppear = !cursorAppear;
  setTimeout(cursorBlink, speed*10);
}


function fadeIn(fadeId) {
  document.getElementById(fadeId).style.display = "";
  document.getElementById(fadeId).style.opacity -= '-.01';

  if(document.getElementById(fadeId).style.opacity < "1"){
    setTimeout(function(){fadeIn(fadeId)}, 10);
  }
}

function fadeOut(fadeId) {
  document.getElementById(fadeId).style.opacity -= '.01';

  if(document.getElementById(fadeId).style.opacity > "0"){
    setTimeout(function(){fadeOut(fadeId)}, 10);
  } else {
    document.getElementById(fadeId).style.display = "none";
  }  
}

async function getComments(limit) {
  fetch('/data?number-comments='+limit)
    .then(response => response.json())
      .then((jsonObject) => {
        document.getElementById('comments').innerHTML = "";
        for(var i = 0; i < jsonObject.length; ++i) {
          document.getElementById('comments').innerHTML += '<p>('+jsonObject[i].timestamp+') '+jsonObject[i].name+': ' +jsonObject[i].message+'</p>';
        }
      });
}

async function deleteComments() {
  await fetch('/delete-data', {
    method: 'POST'
  });
  window.location.href = window.location.href;
}