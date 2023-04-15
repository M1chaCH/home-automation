import { Pipe, PipeTransform } from '@angular/core';
import {LightConfigDTO} from "../../dtos/scene/LightConfigDTO";

@Pipe({
  name: 'rgbsConvert'
})
export class RgbsConvertPipe implements PipeTransform {
  transform(value: LightConfigDTO): string {
    return `rgba(${value.red}, ${value.green}, ${value.blue}, ${value.brightness / 100})`;
  }
}
