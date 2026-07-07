export type Absence = {
  id : string,
  dateTime: string,
  type : AbsenceType,
  isJustified : boolean,
  student : UserInfo,
  element : Element
}

export type AbsenceByElement = {
  element : Element,
  count : number,
  status : AbsenceStatus
}

export enum AbsenceStatus {
  NORMAL,
  VERBAL_WARNING,
  WRITTEN_WARNING,
  DISCIPLINARY_HEARING
}

export type UserInfo = {
  id: string,
  firstName: string,
  lastName: string
}

export enum AbsenceType {
  CLASS,
  EXAM,
  OTHER
}

export type Plan = {
  startsAt: string,
  endsAt : string,
  type : PlanType,
  element : Element,
  room : Room,
}

export type Room = {
  label : string,
  floor : number,
  campus : string
}

export type Element = {
  id : string,
  name : string,
  module : Module,
  teacher : BaseUser
}

export type BaseUser = {
  id : string,
  firstName : string,
  lastName : string
}

export type Module = {
  id : string,
  name : string
}

export enum PlanType {
  LECTURE,
  EXAM
}

export type PlanWithSpan = Plan & {
  rowSpan : number[]
};

export interface Result {
  id: string;
  grade: number;
  student: UserInfo;
  element: Element;
}

export type LoginRequest = {
  usernameOrEmail : string,
  password : string
}

export type User = {
  id : string,
  firstName : string,
  lastName : string,
  username : string,
  email : string,
  dateOfBirth : Date,
  role : Role,
  department : Department,
  studyMode : StudyMode,
  year : number
}

export enum StudyMode {
  HYBRID,
  ON_SITE,
  REMOTE
}

export enum Department {
  GENIE_INFORMATIQUE,
  GENIE_ELECTRIQUE,
  GENIE_CIVIL,
}

export enum Role {
  STUDENT,
  TEACHER,
  ADMIN
}

