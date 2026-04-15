import { KeyValuePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';

type Planning = {
  name : string,
  startsAt: string,
  endsAt : string,
  room : string,
}
type PlanningWithSpan = Planning & {
  rowSpan : number[]
};

type DayPlanning = {
  date : Date,
  plans : Plan[]
}

type Plan = {
  date : Date,
  rowspan : number[],
  name : string,
  room : string,
}

@Component({
  selector: 'app-calendar',
  imports: [KeyValuePipe],
  templateUrl: './calendar.html',
})
export class Calendar implements OnInit {
  week : Date[] = [];

  timeSlots = [
    '07:30','08:00', '08:30','09:00',
    '09:30','10:00', '10:30','11:00',
    '11:30','12:00', '12:30','13:00',
    '13:30','14:00', '14:30','15:00',
    '15:30','16:00', '16:30','17:00',
    '17:30','18:00',
  ];

  days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  timeTable : Plan[] = [];


  ngOnInit(): void {}

  asDate(dateString : string) : Date {
    return new Date(dateString);
  }


  plannings : Record<string, Planning[]>  = {
    "2026/04/06" : [
      { name : "Genie Logiciel", startsAt: "08:00:00", endsAt : "10:30:00", room : "room 204" },
      { name : "Programmation Client Serveur", startsAt: "13:00:00", endsAt : "18:00:00", room : "room 204" },
    ],
    "2026/04/07" : [
      { name : "Java 2", startsAt: "09:00:00", endsAt : "12:00:00", room : "room 101" },
      { name : "SQL Server", startsAt: "15:00:00", endsAt : "17:00:00", room : "room 101" },
    ],
    "2026/04/08" : [
      { name : "Python Data Science", startsAt: "10:30:00", endsAt : "12:00:00", room : "room 101" },
      { name : "UML", startsAt: "14:00:00", endsAt : "16:30:00", room : "room 101" },
    ],
    "2026/04/09" : [
      { name : "Base de données Oracle", startsAt: "08:00:00", endsAt : "11:00:00", room : "room 101" }
    ],
    "2026/04/10" : [
      { name : "Java POO", startsAt: "10:30", endsAt : "12:00:00", room : "TP2" }
    ],
    "2026/04/11" : [],
  }
  generatePlanningWithSpan() : Record<string, PlanningWithSpan[]> {
    let planningsWithSpan : Record<string, PlanningWithSpan[]> = {};
    Object.entries(this.plannings).forEach(([date, plans]) => {
      planningsWithSpan[date] = plans.map(m=> this.addSpan(m));
    });
    return planningsWithSpan;
  }

  addSpan(p : Planning) : PlanningWithSpan {
    const newPlanning : PlanningWithSpan = {
      name : p.name,
      startsAt : p.startsAt,
      endsAt : p.endsAt,
      room : p.room,
      rowSpan : []
    }
    let start = newPlanning.startsAt.substring(0,5);
    let end = newPlanning.endsAt.substring(0,5);
    for(let i=0; i < this.timeSlots.length; i++){
      if(this.timeSlots[i] == start) newPlanning.rowSpan[0] = i;
      if(this.timeSlots[i] == end) newPlanning.rowSpan[1] = i;
    }
    console.log(newPlanning.rowSpan);
    return newPlanning;
  }


  planningWithSplan : Record<string, PlanningWithSpan[]>  = this.generatePlanningWithSpan();
}
