import { Pipe, PipeTransform } from '@angular/core';
import {LightConfigDTO} from "../dtos/scene/LightConfigDTO";

@Pipe({
  name: 'rgbConvert'
})
export class RgbConvertPipe implements PipeTransform {
  transform(value: LightConfigDTO): string {
    return `rgb(${value.red}, ${value.green}, ${value.blue})`;
  }
}
