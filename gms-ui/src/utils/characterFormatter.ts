import * as job from '../assets/job.json';

interface Jobs {
  [key: number]: string;
}

const jobs: Jobs = job as Jobs;

export function formatJob(jobId: number) {
  return jobs.default[jobId]; // 效果： 新手[0]
}

export function formatOther() {
  return null;
}
