import {UserTableFilterPipe} from "./user-table-filter.pipe";
import {of} from "rxjs";

describe('User table Pipe', () => {
  let pipe: UserTableFilterPipe;
  let tableData = {
    data:[
      {
        firstName: 'Tom',
        lastName: 'Sem'
      },
      {
        firstName: 'Tim',
        lastName: 'Alex'
      }
    ]
  };

  beforeEach(() => {

    pipe = new UserTableFilterPipe();
  });

  it('empty search string', (done) => {
    const tableData$ = of({...tableData});
    pipe.transform(tableData$, '').subscribe(e => {
      expect(e.data.length).toBe(2);
      expect(e.data[0].firstName).toBe(tableData.data[0].firstName);
      expect(e.data[0].lastName).toBe(tableData.data[0].lastName);
      expect(e.data[1].firstName).toBe(tableData.data[1].firstName);
      expect(e.data[1].lastName).toBe(tableData.data[1].lastName);
      done()
    });
  });

  it('search for "To"', (done) => {
    const tableData$ = of({...tableData});
    pipe.transform(tableData$, 'To').subscribe(e => {
      expect(e.data.length).toBe(1);
      expect(e.data[0].firstName).toBe(tableData.data[0].firstName);
      expect(e.data[0].lastName).toBe(tableData.data[0].lastName);
      done()
    });
  });

  it('search for "Toa"', (done) => {
    const tableData$ = of({...tableData});
    pipe.transform(tableData$, 'Toa').subscribe(e => {
      expect(e.data.length).toBe(0);
      done()
    });
  })
});
